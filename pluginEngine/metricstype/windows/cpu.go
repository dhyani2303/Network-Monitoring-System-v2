package windows

func Commands() string {

	return "(Get-Counter -Counter \"\\Processor(_Total)\\% Interrupt Time\").CounterSamples.CookedValue\n" + "(Get-Counter -Counter \"\\Processor(_total)\\% Idle Time\").CounterSamples.CookedValue;" +
		"(Get-Counter -Counter \"\\Processor(_Total)\\% User Time\").CounterSamples.CookedValue;" +
		"(Get-WmiObject -Class Win32_Processor | Measure-Object -Property NumberOfCores -Sum).Sum;"

}

func MetricFormatter() {

}
