package plugins

import (
	"PluginEngine/client"
	"PluginEngine/constants"
	"PluginEngine/logger"
	"PluginEngine/utils"
	"fmt"
	"strings"
)

func Discovery(context map[string]interface{}, channel chan map[string]interface{}) {

	errorArray := make([]map[string]interface{}, 0)

	result := make(map[string]interface{})

	client := client.WinRmClient{}

	client.SetContext(context)

	connectionContext, err := client.CreateConnection()

	if err != nil {

		errorArray = append(errorArray, utils.ErrorHandler(constants.CONNECTIONERROR, err.Error()))

		context[constants.Error] = errorArray

		context[constants.Result] = make(map[string]interface{})

		context[constants.Status] = constants.Fail

		channel <- context

		return
	}

	command := "hostname"

	output, errorOutput, exitCode, err := client.ExecuteCommand(connectionContext, command)

	if err != nil {

		errorArray = append(errorArray, utils.ErrorHandler(constants.CONNECTIONERROR, err.Error()))

		context[constants.Error] = errorArray

		context[constants.Result] = result

		context[constants.Status] = constants.Fail

		channel <- context

		return
	}

	if exitCode != 0 {

		errorArray = append(errorArray, utils.ErrorHandler(constants.COMMANDERROR, errorOutput))

		context[constants.Error] = errorArray

		context[constants.Result] = make(map[string]interface{})

		context[constants.Status] = constants.Fail

		channel <- context

	} else {

		result = make(map[string]interface{})

		result[constants.IP] = context[constants.IP]

		result[constants.Hostname] = strings.Trim(output, "/r\n")

		if len(errorArray) > 0 {

			context[constants.Status] = constants.Fail

			context[constants.Result] = result

		} else {

			context[constants.Error] = errorArray

			context[constants.Result] = result

			context[constants.Status] = constants.Success

		}

		channel <- context

		return

	}

}

func Collect(context map[string]interface{}, channel chan map[string]interface{}) {

	logger := logger.NewLogger("plugins", "windows")

	logger.Info("Inside the Collect method")

	networkMetrics := "Get-Counter -Counter \"\\Network Interface(*)\\Output Queue Length\" | Select-Object -ExpandProperty CounterSamples |  Measure-Object -Property CookedValue -Sum | Select-Object @{Name='system.network.output.queue.length';Expression={($_.Sum)}}| fl;" +
		"Get-Counter -Counter \"\\Network Interface(*)\\Packets Sent/sec\"  | Select-Object -ExpandProperty CounterSamples |  Measure-Object -Property CookedValue -Sum | Select-Object @{Name='system.network.out.packets.per.sec';Expression={($_.Sum)}} |fl;" +
		"Get-Counter '\\Network Interface()\\Bytes Total/sec' | Select-Object -ExpandProperty CounterSamples | Measure-Object -Property CookedValue -Sum | Select-Object @{Name='system.network.bytes.per.sec';Expression={($_.Sum)}} |fl;" +
		"Write-Output (\"system.network.tcp.retransmissions:\" + (Get-Counter -Counter \"\\TCPv4\\Segments Retransmitted/sec\").CounterSamples.CookedValue);" +
		"Get-Counter \"\\Network Interface(*)\\Bytes Sent/sec\" | Select-Object -ExpandProperty CounterSamples | Measure-Object -Property CookedValue -Sum | Select-Object @{Name='system.network.out.bytes.per.sec';Expression={($_.Sum)}} |fl;" +
		"Get-Counter \"\\Network Interface(*)\\Packets Received Errors\" | Select-Object -ExpandProperty CounterSamples | Measure-Object -Property CookedValue -Sum | Select-Object @{Name='system.network.error.packets';Expression={($_.Sum)}} |fl;" +
		"Get-Counter -Counter \"\\Network Interface(*)\\Packets Received/sec\" | Select-Object -expandProperty CounterSamples  | Measure-Object -Property CookedValue -Sum | Select-Object @{Name='system.network.in.packets.per.sec';Expression={($_.Sum)}} |fl;" +
		"(Get-Counter -Counter \"\\Network Interface(*)\\Bytes Received/sec\" | Select-Object -ExpandProperty CounterSamples | Measure-Object -Property CookedValue -Sum | Select-Object @{Name='system.network.in.bytes.per.sec';Expression={($_.Sum)}} | fl);" +
		"Write-Output (\"system.network.tcp.connections:\" +  (Get-Counter -Counter \"\\TCPv4\\Connections Established\").CounterSamples.CookedValue);"

	systemMetrics := "Write-Output (\"system.serial.number:\" + (Get-WmiObject Win32_BIOS).SerialNumber);" +
		"Write-Output (\"system.logical.processors:\" + (Get-WmiObject Win32_ComputerSystem).NumberOfLogicalProcessors);" +
		"Write-Output (\"system.virtual:\" +  (Get-WmiObject Win32_ComputerSystem));" +
		"Write-Output (\"system.os.name:\" + (Get-WmiObject Win32_OperatingSystem).Caption);" +
		"Write-Output (\"system.model:\" + (Get-WmiObject Win32_ComputerSystem).Model);" +
		"Write-Output (\"system.os.service.pack:\" + (Get-WMIObject Win32_OperatingSystem).Version);" +
		"Write-Output (\"systems.interrupts.per.sec:\" + (Get-Counter -Counter \"\\Processor(_Total)\\Interrupts/sec\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.vendor:\" + (Get-WmiObject -Class Win32_ComputerSystem).Manufacturer);" +
		"Write-Output (\"system.vendor:\" + (Get-WmiObject -Class Win32_ComputerSystem).Name);" +
		"Write-Output (\"system.threads:\" + (Get-WmiObject -Class Win32_PerfFormattedData_PerfOS_System).Threads);" +
		"Write-Output (\"System.Processor.Queue.Length:\" + (Get-Counter -Counter \"\\System\\Processor Queue Length\").CounterSamples.CookedValue);" +
		"Write-Output (\"Started.time: \"  + ((get-date)- (gcim Win32_OperatingSystem).LastBootUpTime));" +
		"Write-Output (\"system.os.version:\" + (Get-WmiObject -Class Win32_OperatingSystem).Version);" +
		"Write-Output (\"system.physical.processors:\" +(Get-WmiObject -Class Win32_ComputerSystem).NumberOfProcessors);" +
		"Write-Output (\"system.running.processes:\" + (Get-Counter -Counter \"\\System\\Processes\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.context.switches.per.sec:\" +  (Get-WmiObject -Class Win32_PerfFormattedData_PerfOS_System).ContextSwitchesPerSec);"

	memoryMetrics := "Write-Output (\"memory.free.percent:\" + ((Get-WmiObject Win32_OperatingSystem).FreePhysicalMemory / (Get-WmiObject Win32_OperatingSystem).TotalVisibleMemorySize)*100);" +
		"Write-Output (\"system.cache.memory.bytes:\" +  (Get-WmiObject Win32_PerfFormattedData_PerfOS_Memory).CacheBytes);" +
		"Write-Output (\"memory.used.percent:\" + ([Math]::Round(((Get-WmiObject Win32_OperatingSystem).TotalVisibleMemorySize - (Get-WmiObject Win32_OperatingSystem).FreePhysicalMemory) / (Get-WmiObject Win32_OperatingSystem).TotalVisibleMemorySize * 100, 2)));" +
		"Write-Output (\"memory.used.bytes:\" + ((Get-WmiObject Win32_OperatingSystem).TotalVisibleMemorySize - (Get-WmiObject Win32_OperatingSystem).FreePhysicalMemory));" +
		"Write-Output (\"memory.committed.bytes:\" + (Get-Counter -Counter \"\\Memory\\Committed Bytes\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.memory.installed.Bytes:\" + (Get-WmiObject -Class Win32_ComputerSystem | Select-Object -ExpandProperty TotalPhysicalMemory));" +
		"Write-Output (\"system.memory.free.bytes: \"  + (Get-Counter -Counter \"\\Memory\\Free & Zero Page List Bytes\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.pages.per.sec: \"  + (Get-Counter -Counter \"\\Memory\\Pages/sec\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.cpu.percent:\"  + (Get-Counter -Counter \"\\Memory\\Page Faults/sec\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.non.paged.memory.bytes:\" + (Get-Counter -Counter \"\\Memory\\Pool Nonpaged Bytes\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.paged.memory.bytes:\" + (Get-Counter -Counter \"\\Memory\\Pool Paged Bytes\").CounterSamples.CookedValue);" +
		"(Get-Counter -Counter \"\\Memory\\Available Bytes\") | Select-Object -ExpandProperty CounterSamples |  Select-Object @{Name='system.memory.available.bytes';Expression={($_.CookedValue)}}  |fl;"

	cpuMetrics := "(Get-Counter -Counter \"\\Processor(_total)\\% Idle Time\") | Select-Object -expandproperty countersamples |  Select-Object @{Name='ProcessorIdleTimePercent';Expression={($_.CookedValue)}} | Format-List;" +
		"(Get-Counter -Counter \"\\Processor(_Total)\\% Interrupt Time\")| Select-Object -expandproperty countersamples |  Select-Object @{Name='CPUInterruptPercent';Expression={($_.CookedValue)}}  | format-list;" +
		"Write-Output (\"system.cpu.user.percent: \"  + (Get-Counter -Counter \"\\Processor(_Total)\\% User Time\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.cpu.percent:\" + (100- (Get-Counter -Counter \"\\Processor(_Total)\\% Idle Time\").CounterSamples.CookedValue));" +
		"Write-Output (\"system.cpu.description:\" + (Get-WMIObject -Class Win32_Processor).Description);" +
		"Write-Output (\"system.cpu.description:\" + (Get-WMIObject -Class Win32_Processor).Description);" +
		"Write-Output (\"systems.cpu.type:\" + (Get-WmiObject -Class Win32_Processor).Name);" +
		"Write-Output (\"system.cpu.cores:\" + (Get-WmiObject -Class Win32_Processor | Measure-Object -Property NumberOfCores -Sum).Sum);"

	diskMetrics1 := "(Get-Counter -Counter \"\\PhysicalDisk(*)\\Disk Writes/sec\") |  Select-Object -expandproperty countersamples  | Measure-Object -Property CookedValue -Sum | Select-Object @{Name='system.disk.io.write.bytes.per.sec';Expression={($_.Sum)}} | fl;" +
		"Get-WmiObject -Class Win32_LogicalDisk |Select-Object DeviceID, @{Name=\"UsedBytes\"; Expression={[math]::Round(($.Size - $.FreeSpace),3)}} |Measure-Object -Property UsedBytes -Sum  | Select-Object @{Name='system.disk.used.bytes';Expression={($_.Sum)}} | fl;" +
		"(Get-Counter -Counter \"\\PhysicalDisk()\\% Idle Time\")  | Select-Object -expandproperty countersamples | Select-Object @{Name='IOTimePercent';Expression={(100 - $_.CookedValue)}} |  Measure-Object -Property IOTimePercent -Sum | Select-Object @{Name='system.disk.io.time.percent';Expression={($_.Sum)}} | fl;" + "Get-Counter -Counter \"\\PhysicalDisk(*)\\Disk Writes/sec\" | Select-Object -ExpandProperty CounterSamples | Measure-Object -Property CookedValue -Sum | Select-Object @{Name='system.disk.io.write.ops.per.sec';Expression={($_.Sum)}} | fl;" +
		"Get-Counter -Counter \"\\PhysicalDisk(total)\\Avg. Disk Bytes/Transfer\" | Select-Object -expandproperty countersamples  | Select-Object @{Name='DiskIOBytesPerSecond';Expression={($_.CookedValue)}}  | fl;" +
		"Get-WmiObject -Class Win32_LogicalDisk | Select-Object -Property @{Label='Total'; expression={($_.Size)}} | Measure-Object -Property Total -Sum | Select-Object @{Name='system.disk.capacity.bytes';Expression={($_.Sum)}}|fl;" +
		"$diskInfo = Get-CimInstance -Class Win32_LogicalDisk | \n    Select-Object  @{Label='Used'; expression={($_.Size - $_.FreeSpace)}},@{Label='Total'; expression={($_.Size)}} | \n    Measure-Object -Property Used,Total -Sum\n\n$usedSum = $diskInfo | Where-Object { $_.Property -eq 'Used' } | Select-Object -ExpandProperty Sum\n$totalSum = $diskInfo | Where-Object { $_.Property -eq 'Total' } | Select-Object -ExpandProperty Sum\n\n$percentageUsed = ($usedSum / $totalSum) * 100\n\nWrite-Output \"system.disk.used.percent: $percentageUsed\";"

	diskMetrics2 := "Write-Output (\"system.disk.free.bytes:\" + (Get-WmiObject -Class Win32_LogicalDisk | Where-Object {$_.DeviceID -eq 'C:'} | Select-Object -ExpandProperty FreeSpace));" +
		"Write-Output (\"system.disk.io.ops.per.second: \" + (Get-Counter -Counter \"\\PhysicalDisk(_Total)\\Disk Reads/sec\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.disk.io.idle.time.percent: \" + (Get-Counter -Counter \"\\LogicalDisk(_Total)\\% Idle Time\").CounterSamples.CookedValue);" +
		"Write-Output (\"disk.io.read.bytes.per.sec: \"  + (Get-Counter -Counter \"\\PhysicalDisk(_Total)\\Disk Read Bytes/sec\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.disk.io.queue.length:\" + (Get-Counter -Counter \"\\PhysicalDisk(_total)\\Current Disk Queue Length\").CounterSamples.CookedValue);" +
		"Write-Output (\"system.disk.io.read.ops.per.sec:\" +  (Get-Counter -Counter \"\\PhysicalDisk(_Total)\\Disk Reads/sec\").CounterSamples.CookedValue);"

	commands := []string{memoryMetrics, cpuMetrics, diskMetrics1, diskMetrics2, systemMetrics, networkMetrics}

	commandLength := len(commands)

	errorArray := make([]map[string]interface{}, 0)

	internalChannel := make(chan map[string]interface{}, 6)

	var result []map[string]interface{}

	client := client.WinRmClient{}

	client.SetContext(context)

	connectionContext, err := client.CreateConnection()

	if err != nil {

		logger.Error(fmt.Sprintf("Unable to create connection context: %v", err))

		errorArray = append(errorArray, utils.ErrorHandler(constants.CONNECTIONERROR, err.Error()))

		context[constants.Error] = errorArray

		context[constants.Result] = result

		context[constants.Status] = constants.Fail

		channel <- context

		return
	} else {

		for i, command := range commands {

			go func(command string, channel chan map[string]interface{}) {

				logger.Info(fmt.Sprintf("Executing command number: %v", i))

				output, errorOutput, exitCode, err := client.ExecuteCommand(connectionContext, command)

				if err != nil {

					mapToReturn := make(map[string]interface{})

					logger.Error(fmt.Sprintf("Error while making connection for command %v\n", command))

					err := utils.ErrorHandler(constants.CONNECTIONERROR, err.Error())

					mapToReturn[constants.Error] = err

					mapToReturn[constants.Result] = make(map[string]interface{})

					logger.Info(fmt.Sprintf("%v\n", context))

					internalChannel <- mapToReturn

					return

				}

				if exitCode != 0 {

					mapToReturn := make(map[string]interface{})

					logger.Error(fmt.Sprintf("Error while making executing command %v\n", command))

					err := utils.ErrorHandler(constants.COMMANDERROR, errorOutput)

					mapToReturn[constants.Error] = err

					mapToReturn[constants.Result] = make(map[string]interface{})

					channel <- mapToReturn

					return

				} else {

					mapToReturn := make(map[string]interface{})

					result := strings.TrimSpace(output)

					resultSlice := strings.Split(result, "\r\n")

					var cleanedSlice []string

					for _, line := range resultSlice {

						if line != "" {

							cleanedSlice = append(cleanedSlice, line)

						}
					}

					resultMap := make(map[string]interface{})

					for _, element := range cleanedSlice {

						metric := strings.Split(element, ":")

						resultMap[metric[0]] = metric[1]
					}

					mapToReturn[constants.Result] = resultMap

					mapToReturn[constants.Error] = make(map[string]interface{})

					internalChannel <- mapToReturn

				}

			}(command, internalChannel)

		}

		for commandLength > 0 {
			select {
			case output := <-internalChannel:

				errorArray = append(errorArray, output[constants.Error].(map[string]interface{}))

				result = append(result, output[constants.Result].(map[string]interface{}))

				commandLength--
			}
		}

		if len(errorArray) > 0 {

			context[constants.Status] = constants.Fail

			context[constants.Result] = result

			context[constants.Error] = errorArray

		} else {

			context[constants.Error] = errorArray

			context[constants.Result] = result

			context[constants.Status] = constants.Success

		}

		channel <- context

		return
	}

}
