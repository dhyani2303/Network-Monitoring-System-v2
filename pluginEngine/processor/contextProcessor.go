package processor

import (
	"PluginEngine/constants"
	"PluginEngine/logger"
	"PluginEngine/plugins"
	"PluginEngine/utils"
	"fmt"
)

func ProcessContexts(message string, senderChannel chan string) {

	logger := logger.NewLogger("contextProcessor", "processContexts")

	contexts, _ := utils.Decode(message)

	//		contextsLength := len(contexts)

	contextsLength := len(contexts)

	channel := make(chan map[string]interface{}, 1)

	//senderChannel := make(chan string, 1)

	defer func() {

		if err := recover(); err != nil {

			logger.Fatal(fmt.Sprintf("Some panic occurred %v\n", err))

		}
		close(channel)

	}()

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

			//senderChannel <- context

		}

	}

	//pushSocket, err := zmq4.NewSocket(zmq4.PUSH)
	//
	//if err != nil {
	//
	//	logger.Fatal(fmt.Sprintf("error occurred while creating pushSocket %s", err))
	//
	//	return
	//}
	//err = pushSocket.Connect("tcp://localhost:5588")
	//
	//if err != nil {
	//
	//	logger.Fatal(fmt.Sprintf("error occurred while connecting to pushSocket %s", err))
	//
	//	return
	//}
	//
	//	go zmq.Send(senderChannel)

	for contextsLength > 0 {

		select {

		case result := <-channel:

			encodedResult, _ := utils.Encode(result)

			fmt.Sprintf("%v", encodedResult)

			senderChannel <- encodedResult

			//_, err := pushSocket.Send(encodedResult, 0)
			//
			//if err != nil {
			//
			//	logger.Fatal(fmt.Sprintf("Error occurred while sending message to pushSocket %s", err))
			//
			//}

			contextsLength--
		}
	}

}
