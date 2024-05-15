package constants

import "time"

const (
	Username = "username"

	Password = "password"

	IP = "ip"

	DefaultPort float64 = 5985

	Port = "port"

	RequestType = "request.type"

	Discovery = "discovery"

	Collect = "collect"

	Status = "status"

	Success = "success"

	Fail = "fail"

	ErrorCode = "error.code"

	Error = "error"

	ErrorMessage = "error.message"

	Result = "result"

	TimeOut = "request.timeout.nanoseconds"

	DefaultTimeOut = 30 * time.Second

	CredentialProfiles = "credential.profiles"

	CredentialID = "credential.id"

	//error codes
	DECODEERROR = "DECODE01"

	CONNECTIONERROR = "CONNECTION01"

	COMMANDERROR = "COMMAND01"

	ENCODEERROR = "ENCODE01"

	CONTEXTMISSINGERROR = "CONTEXT01"

	INVALIDREQUESTYPE = "REQUESTTYPE01"

	//counter constants

	Hostname = "hostname"

	SystemNetworkOutputQueueLength = "system.network.output.queue.length"

	StartedTimeSeconds = "started.time.seconds"

	SystemNetworkOutPacketsPerSecond = "system.network.out.packets.per.sec"

	SystemDiskIOWriteBytesPerSecond = "system.disk.io.write.bytes.per.sec"

	SystemCPUIdlePercent = "system.cpu.idle.percent"

	SystemDiskUsedBytes = "system.disk.used.bytes"

	SystemMemoryFreePercent = "system.memory.free.percent"

	SystemSerialNumber = "system.serial.number"

	SystemLogicalProcessors = "system.logical.processors"

	SystemVirtual = "system.virtual"

	SystemCacheMemoryBytes = "system.cache.memory.bytes"

	SystemDiskIOTimePercent = "system.disk.io.time.percent"

	SystemOSName = "system.os.name"

	SystemDiskIOWriteOpsPerSecond = "system.disk.io.write.ops.per.sec"

	SystemNetworkBytesPerSec = "system.network.bytes.per.sec"

	SystemMemoryUsedPercent = "system.memory.used.percent"

	SystemModel = "system.model"

	SystemDiskIOBytesPerSec = "system.disk.io.bytes.per.sec"

	SystemAvailableBytes = "system.available.bytes"

	SystemCPUInterruptPercent = "system.cpu.interrupt.percent"

	SystemNetworkOutBytesPerSec = "system.network.out.bytes.per.sec"

	SystemMemoryUsedBytesPerSec = "system.memory.used.bytes"

	SystemNetworkErrorPackets = "system.network.error.packets"

	SystemCPUDescription = "system.cpu.description"

	SystemOSServicePack = "system.os.service.pack"

	SystemInterruptsPerSec = "system.interrupts.per.sec"

	SystemMemoryCommittedBytes = "system.memory.committed.bytes"

	SystemCPUType = "system.cpu.type"

	SystemVendor = "system.vendor"

	SystemName = "system.name"

	SystemProcessorQueueLength = "system.processor.queue.length"

	SystemDiskFreeBytes = "system.disk.free.bytes"

	SystemMemoryInstalledBytes = "system.disk.free.bytes"

	SystemDiskIOOpsPerSec = "system.disk.io.ops.per.sec"

	SystemDiskIOIdleTimePercent = "system.disk.idle.time.percent"

	SystemMemoryFreeBytes = "system.memory.free.Bytes"

	SystemPagesPerSec = "system.pages.per.sec"

	StartedTime = "started.time"

	SystemDiskIOReadBytesPerSec = "system.disk.io.read.bytes.per.sec"

	SystemCPUPercent = "system.cpu.percent"

	SystemPagesFaultPerSec = "system.pages.fault.per.sec"

	SystemNetworkTCPTransmissions = "system.network.tcp.retransmissions"

	SystemOSVersion = "system.os.version"

	SystemPhysicalProcessors = "system.physical.processors"

	SystemRunningProcesses = "system.running.processes"

	SystemDiskIOQueueLength = "system.disk.io.queue.length"

	SystemCPUCores = "system.cpu.cores"

	SystemNonPagedMemoryBytes = "system.non.paged.memory.bytes"

	SystemPagedMemoryBytes = "system.paged.memory.bytes"

	SystemNetworkInPacketsPerSec = "system.network.in.packets.per.sec"

	SystemNetworkInBytesPerSec = "system.network.in.bytes.per.sec"

	SystemDiskFreePercent = "system.disk.free.percent"

	SystemNetworkTCPConnections = "system.network.tcp.connections"

	SystemContextSwitchesPerSec = "system.context.switches.per.sec"

	SystemDiskIOReadOpsPerSec = "system.disk.io.read.ops.per.sec"

	SystemDiskCapacityBytes = "system.disk.capacity.bytes"
)
