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

	defer func() {

		if err := recover(); err != nil {

			logger.Fatal(fmt.Sprintf("Some panic occurred %v\n", err))

		}

		close(channel)
	}()

	logger.Info("New Request has come to plugin engine")

	if len(os.Args) < 2 {

		logger.Fatal(constants.CONTEXT_MISSING_ERROR + " Context is empty")

		context := make(map[string]interface{}, 1)

		errors := make([]map[string]interface{}, 1)

		errors = append(errors, utils.ErrorHandler(constants.CONTEXT_MISSING_ERROR, "context is empty"))

		context[constants.ERROR] = errors

		context[constants.STATUS] = constants.FAIL

		result, err := utils.Encode(context)

		if err != nil {

			logger.Fatal(fmt.Sprintf("ERROR while encoding context: %v error:%v", context, err))

		} else {

			fmt.Println(result)

		}

		return

	}

	contexts, err := utils.Decode(os.Args[1])

	contextsLength := len(contexts)

	if err != nil {

		logger.Fatal(fmt.Sprintf("Some error occurred during decoding the context %v", err))

		context := make(map[string]interface{}, 1)

		errors := make([]map[string]interface{}, 0)

		errors = append(errors, utils.ErrorHandler(constants.DECODE_ERROR, err.Error()))

		context[constants.STATUS] = constants.FAIL

		context[constants.RESULT] = make([]map[string]interface{}, 0)

		result, err := utils.Encode(result)

		if err != nil {

			logger.Fatal(fmt.Sprintf("ERROR occurred while encoding the context: %v,error : %v ", context, err))

			return
		}
		fmt.Println(result)

		return

	}

	for _, context := range contexts {

		logger.Info(fmt.Sprintf("Context: %s", context))

		if context[constants.REQUEST_TYPE] == constants.DISCOVERY {

			logger.Info(fmt.Sprintf("Request Type of the context is discovery"))

			go plugins.Discovery(context, channel)

		} else if context[constants.REQUEST_TYPE] == constants.COLLECT {

			logger.Info(fmt.Sprintf("Request Type of the context is collect"))

			go plugins.Collect(context, channel)

		} else {

			logger.Info(fmt.Sprintf("Context does not have valid request type"))

			errors := make([]map[string]interface{}, 0)

			errors = append(errors, utils.ErrorHandler(constants.INVALID_REQUEST_TYPE, "request.type is invalid"))

			context[constants.STATUS] = constants.FAIL

			context[constants.ERROR] = errors

			context[constants.RESULT] = make([]map[string]interface{}, 0)

			channel <- context

		}

	}

	for contextsLength > 0 {

		select {

		case result := <-channel:

			encodedResult, _ := utils.Encode(result)

			fmt.Print(encodedResult + constants.SEPERATOR)

			contextsLength--
		}
	}

}
