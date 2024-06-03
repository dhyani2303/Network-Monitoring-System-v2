package plugin

import (
	"PluginEngine/src/client"
	. "PluginEngine/src/consts"
	utils2 "PluginEngine/src/utils"
	"fmt"
	"maps"
	"math"
	"strconv"
	"strings"
	"sync"
	"time"
)

var logger = utils2.NewLogger("plugins", "windows")

func Discovery(context map[string]interface{}, channel chan map[string]interface{}) {

	errors := make([]interface{}, 0) // errors store map[string]interface{} and this map stores errorCode as well as errorMessage. errorCode are custom made

	result := make(map[string]interface{})

	client := client.WinRmClient{}

	defer func() {

		if err := recover(); err != nil {

			logger.Fatal(fmt.Sprintf("Some panic occurred %v\n", err))

		}
		return

	}()

	logger.Info(fmt.Sprintf("Inside the Discover Method"))

	if credentialProfiles, ok := context[CredentialProfiles].([]interface{}); ok {

		for _, credential := range credentialProfiles {

			if credentialProfile, ok := credential.(map[string]interface{}); ok {

				credentialProfile[IpAddress] = context[IpAddress]

				if context[Port] != nil {

					credentialProfile[Port] = context[Port]
				}

				if context[Timeout] != nil {

					credentialProfile[Timeout] = context[Timeout]
				}

				client.SetContext(credentialProfile)
			}

			connection, err := client.CreateConnection()

			if err != nil {

				errors = append(errors, utils2.ErrorHandler(ConnectionError, err.Error()))

				logger.Error(fmt.Sprintf("Error occurred %v", err))

				continue

			} else {
				command := "hostname"

				output, errorOutput, exitCode, err := client.ExecuteCommand(connection, command)

				if err != nil {

					errors = append(errors, utils2.ErrorHandler(ConnectionError, err.Error()))

					logger.Error(fmt.Sprintf("Error occurred %v\n", err))

					continue

				} else if exitCode != 0 {

					errors = append(errors, utils2.ErrorHandler(CommandError, errorOutput))

					logger.Error(fmt.Sprintf("Error occurred %v\n", errorOutput))

					continue

				} else {

					if id, ok := credential.(map[string]interface{})[CredentialId]; ok {

						context[ValidCredentialId] = id
					}
					result[IpAddress] = context[IpAddress]

					result[Hostname] = strings.Trim(output, "\r\n")

					break

				}
			}

		}
		if len(result) > 0 {

			context[Status] = Success

		} else {

			context[Status] = Fail

		}
		context[Result] = result

		context[Error] = errors

	}
	channel <- context

	return

}

func Collect(context map[string]interface{}, channel chan map[string]interface{}) {

	logger.Trace("Collector method has been called")

	defer func() {

		if err := recover(); err != nil {

			logger.Fatal(fmt.Sprintf("Some panic occurred %v\n", err))

		}

	}()

	cpuMetrics := "(Get-Counter -Counter \"\\Processor(_total)\\% Idle Time\") | Select-Object -ExpandProperty CounterSamples |  Select-Object @{Name='system.cpu.idle.percent';Expression={($_.CookedValue)}} | fl;" +
		"(Get-Counter -Counter \"\\Processor(_Total)\\% Interrupt Time\")| Select-Object -ExpandProperty CounterSamples |  Select-Object @{Name='system.cpu.interrupt.percent';Expression={($_.CookedValue)}}  | format-list;" +
		"Write-Output (\"system.cpu.user.percent:\"  + (Get-Counter -Counter \"\\Processor(_Total)\\% User Time\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.cpu.percent:\" + (100- (Get-Counter -Counter \"\\Processor(_Total)\\% Idle Time\").CounterSamples.CookedValue));" +
		"Write-Output (\"system.cpu.description:\" + (Get-WMIObject -Class Win32_Processor).Description);" +
		"Write-Output (\"systems.cpu.type:\" + (Get-WmiObject -Class Win32_Processor).Name);" +
		"Write-Output (\"system.cpu.cores:\" + (Get-WmiObject -Class Win32_Processor | Measure-Object -Property NumberOfCores -Sum).Sum);"

	// Disk Metrics
	diskMetrics2 := "Write-Output (\"system.disk.free.bytes:\" + (Get-WmiObject -Class Win32_LogicalDisk | Where-Object {$_.DeviceID -eq 'C:'} | Select-Object -ExpandProperty FreeSpace));" +
		"Write-Output (\"system.disk.io.ops.per.second:\" + (Get-Counter -Counter \"\\PhysicalDisk(_Total)\\Disk Reads/sec\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.disk.io.idle.time.percent:\" + (Get-Counter -Counter \"\\LogicalDisk(_Total)\\% Idle Time\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.disk.io.read.bytes.per.sec:\"  + (Get-Counter -Counter \"\\PhysicalDisk(_Total)\\Disk Read Bytes/sec\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.disk.io.queue.length:\" + (Get-Counter -Counter \"\\PhysicalDisk(_total)\\Current Disk Queue Length\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.disk.io.read.ops.per.sec:\" +  (Get-Counter -Counter \"\\PhysicalDisk(_Total)\\Disk Reads/sec\").CounterSamples.CookedValue);"

	diskMetrics1 := "(Get-Counter -Counter \"\\PhysicalDisk(*)\\Disk Writes/sec\") |  Select-Object -ExpandProperty CounterSamples  | Measure-Object -Property CookedValue -Sum | Select-Object @{Name='system.disk.io.write.bytes.per.sec';Expression={($_.Sum)}} | fl;" +
		"Get-WmiObject -Class Win32_LogicalDisk |Select-Object DeviceID, @{Name=\"UsedBytes\"; Expression={[math]::Round(($.Size - $.FreeSpace),3)}} |Measure-Object -Property UsedBytes -Sum  | Select-Object @{Name='system.disk.used.bytes';Expression={($_.Sum)}} | fl;" +
		"(Get-Counter -Counter \"\\PhysicalDisk(*)\\Disk Writes/sec\") | Select-Object -ExpandProperty CounterSamples | Measure-Object -Property CookedValue -Sum | Select-Object @{Name='system.disk.io.write.ops.per.sec';Expression={($_.Sum)}} | fl;" +
		"(Get-Counter -Counter \"\\PhysicalDisk(_total)\\Avg. Disk Bytes/Transfer\") | Select-Object -ExpandProperty CounterSamples  | Select-Object @{Name='system.disk.io.bytes.per.sec';Expression={($_.CookedValue)}}  | fl;" +
		"Get-WmiObject -Class Win32_LogicalDisk | Select-Object -Property @{Label='Total'; expression={($_.Size)}} | Measure-Object -Property Total -Sum | Select-Object @{Name='system.disk.capacity.bytes';Expression={($_.Sum)}}|fl;" +
		"$diskInfo = Get-WmiObject Win32_LogicalDisk\n$totalFreeSpace = ($diskInfo | Measure-Object -Property FreeSpace -Sum).Sum\n$totalSize = ($diskInfo | Measure-Object -Property Size -Sum).Sum\n$totalFreePercentage = ($totalFreeSpace / $totalSize) * 100\nWrite-Output \"system.disk.free.percent: $totalFreePercentage\";" +
		"$diskInfo = Get-CimInstance -Class Win32_LogicalDisk | \n    Select-Object  @{Label='Used'; expression={($_.Size - $_.FreeSpace)}},@{Label='Total'; expression={($_.Size)}} | \n    Measure-Object -Property Used,Total -Sum\n\n$usedSum = $diskInfo | Where-Object { $_.Property -eq 'Used' } | Select-Object -ExpandProperty Sum\n$totalSum = $diskInfo | Where-Object { $_.Property -eq 'Total' } | Select-Object -ExpandProperty Sum\n\n$percentageUsed = ($usedSum / $totalSum) * 100\n\nWrite-Output \"system.disk.used.percent: $percentageUsed\";"

	systemMetrics := "Write-Output (\"system.serial.number:\" + (Get-WmiObject Win32_BIOS).SerialNumber);" +
		"Write-Output (\"system.started.time.seconds:\" + (((get-date)- (gcim Win32_OperatingSystem).LastBootUpTime).totalSeconds));" +
		"Write-Output (\"system.logical.processors:\" + (Get-WmiObject Win32_ComputerSystem).NumberOfLogicalProcessors);" +
		"Write-Output (\"system.virtual:\" +  (Get-WmiObject Win32_ComputerSystem));" +
		"Write-Output (\"system.os.name:\" + (Get-WmiObject Win32_OperatingSystem).Caption);" +
		"Write-Output (\"system.model:\" + (Get-WmiObject Win32_ComputerSystem).Model);" +
		"Write-Output (\"system.os.service.pack:\" + (Get-WMIObject Win32_OperatingSystem).Version);" +
		"Write-Output (\"system.interrupts.per.sec:\" + (Get-Counter -Counter \"\\Processor(_Total)\\Interrupts/sec\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.vendor:\" + (Get-WmiObject -Class Win32_ComputerSystem).Manufacturer);" +
		"Write-Output (\"system.name:\" + (Get-WmiObject -Class Win32_ComputerSystem).Name);" +
		"Write-Output (\"system.threads:\" + (Get-WmiObject -Class Win32_PerfFormattedData_PerfOS_System).Threads);" +
		"Write-Output (\"system.processor.queue.length:\" + (Get-Counter -Counter \"\\System\\Processor Queue Length\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.started.time: \"  + ((get-date)- (gcim Win32_OperatingSystem).LastBootUpTime));" +
		"Write-Output (\"system.os.version:\" + (Get-WmiObject -Class Win32_OperatingSystem).Version);" +
		"Write-Output (\"system.physical.processors:\" +(Get-WmiObject -Class Win32_ComputerSystem).NumberOfProcessors);" +
		"Write-Output (\"system.running.processes:\" + (Get-Counter -Counter \"\\System\\Processes\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.context.switches.per.sec:\" +  (Get-WmiObject -Class Win32_PerfFormattedData_PerfOS_System).ContextSwitchesPerSec);"

	networkMetrics := "Get-Counter -Counter \"\\Network Interface(*)\\Output Queue Length\" | Select-Object -ExpandProperty CounterSamples |  Measure-Object -Property CookedValue -Sum | Select-Object @{Name='system.network.output.queue.length';Expression={($_.Sum)}}| fl;" +
		"Get-Counter -Counter \"\\Network Interface(*)\\Packets Sent/sec\"  | Select-Object -ExpandProperty CounterSamples |  Measure-Object -Property CookedValue -Sum | Select-Object @{Name='system.network.out.packets.per.sec';Expression={($_.Sum)}} |fl;" +
		"Get-Counter '\\Network Interface(*)\\Bytes Total/sec' | Select-Object -ExpandProperty CounterSamples | Measure-Object -Property CookedValue -Sum | Select-Object @{Name='system.network.bytes.per.sec';Expression={($_.Sum)}} |fl;" +
		"Write-Output (\"system.network.tcp.retransmissions:\" + (Get-Counter -Counter \"\\TCPv4\\Segments Retransmitted/sec\").CounterSamples.CookedValue);" +
		"Get-Counter \"\\Network Interface(*)\\Bytes Sent/sec\" | Select-Object -ExpandProperty CounterSamples | Measure-Object -Property CookedValue -Sum | Select-Object @{Name='system.network.out.bytes.per.sec';Expression={($_.Sum)}} |fl;" +
		"Get-Counter \"\\Network Interface(*)\\Packets Received Errors\" | Select-Object -ExpandProperty CounterSamples | Measure-Object -Property CookedValue -Sum | Select-Object @{Name='system.network.error.packets';Expression={($_.Sum)}} |fl;" +
		"Get-Counter -Counter \"\\Network Interface(*)\\Packets Received/sec\" | Select-Object -expandProperty CounterSamples  | Measure-Object -Property CookedValue -Sum | Select-Object @{Name='system.network.in.packets.per.sec';Expression={($_.Sum)}} |fl;" +
		"(Get-Counter -Counter \"\\Network Interface(*)\\Bytes Received/sec\" | Select-Object -ExpandProperty CounterSamples | Measure-Object -Property CookedValue -Sum | Select-Object @{Name='system.network.in.bytes.per.sec';Expression={($_.Sum)}} | fl);" +
		"Write-Output (\"system.network.tcp.connections:\" +  (Get-Counter -Counter \"\\TCPv4\\Connections Established\").CounterSamples.CookedValue);"

	memoryMetrics := "Write-Output (\"system.memory.free.percent:\" + ((Get-WmiObject Win32_OperatingSystem).FreePhysicalMemory / (Get-WmiObject Win32_OperatingSystem).TotalVisibleMemorySize)*100);" +
		"Write-Output (\"system.cache.memory.bytes:\" +  (Get-WmiObject Win32_PerfFormattedData_PerfOS_Memory).CacheBytes);" +
		"Write-Output (\"system.memory.used.percent:\" + ([Math]::Round(((Get-WmiObject Win32_OperatingSystem).TotalVisibleMemorySize - (Get-WmiObject Win32_OperatingSystem).FreePhysicalMemory) / (Get-WmiObject Win32_OperatingSystem).TotalVisibleMemorySize * 100, 2)));" +
		"Write-Output (\"system.memory.used.bytes:\" + ((Get-WmiObject Win32_OperatingSystem).TotalVisibleMemorySize - (Get-WmiObject Win32_OperatingSystem).FreePhysicalMemory));" +
		"Write-Output (\"system.memory.committed.bytes:\" + (Get-Counter -Counter \"\\Memory\\Committed Bytes\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.memory.installed.bytes:\" + (Get-WmiObject -Class Win32_ComputerSystem | Select-Object -ExpandProperty TotalPhysicalMemory));" +
		"Write-Output (\"system.memory.free.bytes: \"  + (Get-Counter -Counter \"\\Memory\\Free & Zero Page List Bytes\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.pages.per.sec: \"  + (Get-Counter -Counter \"\\Memory\\Pages/sec\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.pages.faults.per.sec:\"  + (Get-Counter -Counter \"\\Memory\\Page Faults/sec\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.non.paged.memory.bytes:\" + (Get-Counter -Counter \"\\Memory\\Pool Nonpaged Bytes\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.paged.memory.bytes:\" + (Get-Counter -Counter \"\\Memory\\Pool Paged Bytes\").CounterSamples.CookedValue);" +
		"(Get-Counter -Counter \"\\Memory\\Available Bytes\") | Select-Object -ExpandProperty CounterSamples |  Select-Object @{Name='system.memory.available.bytes';Expression={($_.CookedValue)}} |fl;"

	commands := []string{memoryMetrics, cpuMetrics, diskMetrics1, diskMetrics2, systemMetrics, networkMetrics}

	commandLength := len(commands)

	errors := make([]map[string]interface{}, 0)

	notification := make(chan map[string]interface{}, 6)

	result := make(map[string]interface{})

	var wg sync.WaitGroup

	for _, command := range commands {

		wg.Add(1)

		go func(command string, channel chan map[string]interface{}) {

			client := client.WinRmClient{}

			client.SetContext(context)

			connection, err := client.CreateConnection()

			if err != nil {

				response := make(map[string]interface{})

				logger.Error(fmt.Sprintf("Unable to create connection context: %v", err))

				errors = append(errors, utils2.ErrorHandler(ConnectionError, err.Error()))

				response[Error] = errors

				response[Result] = make(map[string]interface{})

				notification <- response

				return
			}

			defer wg.Done()

			logger.Info(fmt.Sprintf("Executing command with command name : %v", command))

			output, errorOutput, exitCode, err := client.ExecuteCommand(connection, command)

			if err != nil {

				response := make(map[string]interface{})

				logger.Error(fmt.Sprintf("Error while making connection for command %v\n", command))

				err := utils2.ErrorHandler(ConnectionError, err.Error())

				response[Error] = err

				response[Result] = make(map[string]interface{})

				logger.Info(fmt.Sprintf("%v\n", context))

				notification <- response

				return

			}

			if exitCode != 0 {

				response := make(map[string]interface{})

				logger.Error(fmt.Sprintf("Error while making executing command %v\n", command))

				err := utils2.ErrorHandler(CommandError, errorOutput)

				response[Error] = err

				response[Result] = make(map[string]interface{})

				notification <- response

				return

			} else {

				response := make(map[string]interface{})

				output := strings.TrimSpace(output)

				lines := strings.Split(output, "\r\n")

				var cleanedResult []string

				for _, line := range lines {

					if line != "" {

						cleanedResult = append(cleanedResult, line)

					}
				}

				result := make(map[string]interface{})

				for _, element := range cleanedResult {

					metric := strings.SplitN(element, ":", 2)

					metric[0] = strings.TrimSpace(metric[0])

					metric[1] = strings.TrimSpace(metric[1])

					if utils2.MetricsMap[metric[0]] == "Count" {

						value, err := strconv.ParseFloat(metric[1], 64)

						if err != nil {

							logger.Error(fmt.Sprintf("cannot convert to float %v\n", err))

						} else {

							result[metric[0]] = math.Round(value*100) / 100

						}
					} else {

						result[metric[0]] = metric[1]
					}

				}

				response[Result] = result

				response[Error] = ""

				notification <- response

			}

			return

		}(command, notification)

	}

	go func() {
		wg.Wait()

		close(notification)

		return
	}()

	for commandLength > 0 {

		select {

		case output := <-notification:

			if output[Error] != "" {

				errors = append(errors, output[Error].(map[string]interface{}))
			}

			maps.Copy(result, output[Result].(map[string]interface{}))

			commandLength--
		}
	}

	if len(errors) > 0 {

		context[Status] = Fail

	} else {

		context[Status] = Success

	}

	context[Timestamp] = time.Now().UnixMilli()

	context[Result] = result

	context[Error] = errors

	channel <- context

	return
}
