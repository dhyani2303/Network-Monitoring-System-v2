package main

import (
	"PluginEngine/constants"
	"PluginEngine/logger"
	"PluginEngine/plugins"
	"PluginEngine/utils"
	"fmt"
	"os"
)

func main() {

	result := make(map[string]interface{})

	channel := make(chan map[string]interface{}, 1)

	logger := logger.NewLogger("bootstrap", "main")

	logger.Info("Plugin Engine Initialized")

	if len(os.Args) < 2 {

		logger.Fatal(constants.CONTEXTMISSINGERROR + " Context is empty")

		context := make(map[string]interface{}, 1)

		utils.ErrorHandler(context, constants.CONTEXTMISSINGERROR, fmt.Errorf("context is empty"))

		result, err := utils.Encode(context)

		if err != nil {

			logger.Fatal(fmt.Sprintf("Error while encoding context: %v", err))

			utils.ErrorHandler(context, constants.ENCODEERROR, err)

			fmt.Println(context)

		} else {

			//unsure how to handle the case when encoding fails

			fmt.Println(result)

		}

		return

	}

	contextData, err := utils.Decode(os.Args[1])

	contextDataLength := len(contextData)

	if err != nil {

		logger.Fatal(fmt.Sprintf("Some error occurred during decoding the context %v", err))

		utils.ErrorHandler(result, constants.DECODEERROR, err)

		encodedResult, err := utils.Encode(result)

		if err != nil {

			utils.ErrorHandler(result, constants.ENCODEERROR, err)

			fmt.Println(result)
		}
		fmt.Println(encodedResult)

		return

	}

	for _, context := range contextData {

		logger.Info(fmt.Sprintf("Context: %s", context))

		if context[constants.RequestType] == constants.Discovery {

			go plugins.Discovery(context, channel)

		} else if context[constants.RequestType] == constants.Collect {

			//	go plugins.Collect(context)

		} else {

			utils.ErrorHandler(context, constants.INVALIDREQUESTYPE, fmt.Errorf("request.type is invalid"))

			channel <- context

		}

	}

	for contextDataLength > 0 {
		select {
		case result := <-channel:

			encodedResult, _ := utils.Encode(result)

			fmt.Println(encodedResult)

			contextDataLength--
		}
	}

}
