package main

import (
	"PluginEngine/logger"
	"PluginEngine/processor"
	"PluginEngine/zmq"
	"fmt"
)

func main() {

	logger := logger.NewLogger("bootstrap", "main")

	logger.Info("New Request has come to plugin engine")

	//	result := make(map[string]interface{})

	senderChannel := make(chan string)

	//channel := make(chan map[string]interface{}, 1)

	receiverChannel := make(chan string, 1)

	defer func() {

		if err := recover(); err != nil {

			logger.Fatal(fmt.Sprintf("Some panic occurred %v\n", err))

		}

		close(receiverChannel)

		close(senderChannel)

	}()

	go zmq.Receive(receiverChannel)

	go zmq.Send(senderChannel)

	//pullSocket, err := zmq4.NewSocket(zmq4.PULL)
	//
	//defer func(responder *zmq4.Socket) {
	//
	//	err := responder.Close()
	//
	//	if err != nil {
	//
	//		logger.Fatal(fmt.Sprintf("error occurred while closing pullSocket %s", err))
	//
	//	}
	//}(pullSocket)
	//
	//if err != nil {
	//
	//	logger.Fatal(fmt.Sprintf("error occurred while creating pullSocket %s", err))
	//}
	//
	//err = pullSocket.Connect("tcp://localhost:5587")
	//
	//if err != nil {
	//
	//	logger.Fatal(fmt.Sprintf("error occurred while connecting to pullSocket %s", err))
	//}

	for {
		select {

		case message := <-receiverChannel:

			//message, err := pullSocket.Recv(0)
			//
			//fmt.Println(message)
			//
			//if err != nil {
			//
			//	logger.Fatal(fmt.Sprintf("error occurred while receiving message from pullSocket %s", err))
			//}

			//if len(os.Args) < 2 {
			//
			//	logger.Fatal(constants.CONTEXT_MISSING_ERROR + " Context is empty")
			//
			//	context := make(map[string]interface{}, 1)
			//
			//	errors := make([]map[string]interface{}, 1)
			//
			//	errors = append(errors, utils.ErrorHandler(constants.CONTEXT_MISSING_ERROR, "context is empty"))
			//
			//	context[constants.ERROR] = errors
			//
			//	context[constants.STATUS] = constants.FAIL
			//
			//	result, err := utils.Encode(context)
			//
			//	if err != nil {
			//
			//		logger.Fatal(fmt.Sprintf("ERROR while encoding context: %v error:%v", context, err))
			//
			//	} else {
			//
			//		fmt.Println(result)
			//
			//	}
			//
			//	return
			//
			//}

			//	contexts, err := utils.Decode(message)

			//		contextsLength := len(contexts)

			//if err != nil {
			//
			//	logger.Fatal(fmt.Sprintf("Some error occurred during decoding the context %v", err))
			//
			//	context := make(map[string]interface{}, 1)
			//
			//	errors := make([]map[string]interface{}, 0)
			//
			//	errors = append(errors, utils.ErrorHandler(constants.DECODE_ERROR, err.Error()))
			//
			//	context[constants.STATUS] = constants.FAIL
			//
			//	context[constants.RESULT] = make([]map[string]interface{}, 0)
			//
			//	result, err := utils.Encode(result)
			//
			//	if err != nil {
			//
			//		logger.Fatal(fmt.Sprintf("ERROR occurred while encoding the context: %v,error : %v ", context, err))
			//
			//		return
			//	}
			//	fmt.Println(result)
			//
			//	return
			//
			//}

			go processor.ProcessContexts(message, senderChannel)

			//for _, context := range contexts {
			//
			//	logger.Info(fmt.Sprintf("Context: %s", context))
			//
			//	if context[constants.REQUEST_TYPE] == constants.DISCOVERY {
			//
			//		logger.Info(fmt.Sprintf("Request Type of the context is discovery"))
			//
			//		go plugins.Discovery(context, channel)
			//
			//	} else if context[constants.REQUEST_TYPE] == constants.COLLECT {
			//
			//		logger.Info(fmt.Sprintf("Request Type of the context is collect"))
			//
			//		go plugins.Collect(context, channel)
			//
			//	} else {
			//
			//		logger.Info(fmt.Sprintf("Context does not have valid request type"))
			//
			//		errors := make([]map[string]interface{}, 0)
			//
			//		errors = append(errors, utils.ErrorHandler(constants.INVALID_REQUEST_TYPE, "request.type is invalid"))
			//
			//		context[constants.STATUS] = constants.FAIL
			//
			//		context[constants.ERROR] = errors
			//
			//		context[constants.RESULT] = make([]map[string]interface{}, 0)
			//
			//		channel <- context
			//
			//	}
			//
			//}
			//
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
			//for contextsLength > 0 {
			//
			//	select {
			//
			//	case result := <-channel:
			//
			//		encodedResult, _ := utils.Encode(result)
			//
			//		_, err := pushSocket.Send(encodedResult, 0)
			//
			//		if err != nil {
			//
			//			logger.Fatal(fmt.Sprintf("Error occurred while sending message to pushSocket %s", err))
			//
			//		}
			//
			//		contextsLength--
			//	}
		}

	}
}
