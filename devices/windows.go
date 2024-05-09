package devices

import (
	"PluginEngine/client"
	"PluginEngine/metricstype/windows"
	"PluginEngine/utility"
	"PluginEngine/utility/constants"
	"fmt"
	"log"
)

func Windows(context map[string]interface{}) {

	switch context[constants.RequestType] {

	case constants.Discovery:

		Discovery(context)

	case constants.Collect:

		Collect(context)
	}
}

func Discovery(context map[string]interface{}) {

	err := client.CreateConnection(context)

	if err == nil {

		_, err, exitCode, errorCause := client.ExecuteCommand("ipconfig")

		if exitCode != 0 && err != nil {

			utility.ErrorHandler(context, constants.DISCOVERYERROR, err)

			log.Println("Error in discovery", errorCause, " ", err)
			_ = errorCause

		} else {

			result := make(map[string]interface{})

			result[constants.IP] = context[constants.IP]

			context[constants.Status] = constants.Success

			context[constants.Result] = result

		}

	} else {

		utility.ErrorHandler(context, constants.DISCOVERYERROR, err)

		log.Println("Error in discovery", err)
	}

}

func Collect(context map[string]interface{}) {

	err := client.CreateConnection(context)
	if err == nil {
		switch context[constants.MetricGroup] {
		case constants.CPU:

			output, err, exitCode, errorCause := client.ExecuteCommand(windows.Commands())

			fmt.Println(output, err, exitCode, errorCause)

		}
	}
}
