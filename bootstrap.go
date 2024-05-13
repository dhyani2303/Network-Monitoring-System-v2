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

		errorArray := make([]map[string]interface{}, 1)

		errorArray = append(errorArray, utils.ErrorHandler(constants.CONTEXTMISSINGERROR, "context is empty"))

		context[constants.Error] = errorArray

		context[constants.Status] = constants.Fail

		result, err := utils.Encode(context)

		if err != nil {

			logger.Fatal(fmt.Sprintf("Error while encoding context: %v", err))

			errorArray = append(errorArray, utils.ErrorHandler(constants.ENCODEERROR, err.Error()))

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

		context := make(map[string]interface{}, 1)

		errorArray := make([]map[string]interface{}, 0)

		errorArray = append(errorArray, utils.ErrorHandler(constants.DECODEERROR, err.Error()))

		context[constants.Status] = constants.Fail

		context[constants.Result] = make([]map[string]interface{}, 0)

		encodedResult, err := utils.Encode(result)

		if err != nil {

			utils.ErrorHandler(constants.ENCODEERROR, err.Error())

			fmt.Println(context)

			return
		}
		fmt.Println(encodedResult)

		return

	}

	for _, context := range contextData {

		logger.Info(fmt.Sprintf("Context: %s", context))

		if context[constants.RequestType] == constants.Discovery {

			go plugins.Discovery(context, channel)

		} else if context[constants.RequestType] == constants.Collect {

			go plugins.Collect(context, channel)

		} else {

			errorArray := make([]map[string]interface{}, 0)

			errorArray = append(errorArray, utils.ErrorHandler(constants.INVALIDREQUESTYPE, "request.type is invalid"))

			context[constants.Status] = constants.Fail

			context[constants.Result] = make([]map[string]interface{}, 0)

			channel <- context

		}

	}

	for contextDataLength > 0 {

		select {

		case result := <-channel:

			encodedResult, _ := utils.Encode(result)

			fmt.Print(encodedResult)

			fmt.Print("||@@||")

			contextDataLength--
		}
	}

	defer func() {

		close(channel)
	}()

}
