package plugins

import (
	"PluginEngine/client"
	"PluginEngine/constants"
	"PluginEngine/utils"
)

func Discovery(context map[string]interface{}, channel chan map[string]interface{}) {

	client := client.WinRmClient{}

	client.SetContext(context)

	connectionContext, err := client.CreateConnection()

	if err != nil {

		utils.ErrorHandler(context, constants.CONNECTIONERROR, err)

		return
	}

	command := "hostname"

	output, _, exitCode, err := client.ExecuteCommand(connectionContext, command)

	if exitCode != 0 {

		utils.ErrorHandler(context, constants.COMMANDERROR, err)

		channel <- context

		return

	}

	result := make(map[string]interface{})

	result[constants.IP] = context[constants.IP]

	result[constants.Hostname] = output

	context[constants.Result] = result

	context[constants.Status] = constants.Success

	channel <- context

}

//func Collect(context map[string]interface{}) {
//
//	err := client.CreateConnection(context)
//	if err == nil {
//		switch context[constants.MetricGroup] {
//		case constants.CPU:
//
//			output, err, exitCode, errorCause := client.ExecuteCommand(windows.Commands())
//
//			fmt.Println(output, err, exitCode, errorCause)
//
//		}
//	}
//}
