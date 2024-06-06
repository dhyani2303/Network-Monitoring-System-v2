package main

import (
	. "PluginEngine/src/consts"
	"PluginEngine/src/plugin"
	"PluginEngine/src/server"
	"PluginEngine/src/utils"
	"fmt"
)

var logger = utils.NewLogger("main", "bootstrap")

var senderChannel = make(chan string, 20)

var receiverChannel = make(chan string, 20)

var channel = make(chan map[string]interface{}, 1)

func main() {

	logger.Info("New Request has come to plugin engine")

	sockets := server.Sockets{}

	defer func() {

		if err := recover(); err != nil {

			logger.Fatal(fmt.Sprintf("Some panic occurred %v\n", err))

		}

		close(receiverChannel)

		close(senderChannel)

		close(channel)

		return
	}()

	err := sockets.Init()

	if err != nil {

		logger.Error(fmt.Sprintf("Some error occurred while initialising sockets %v\n", err))

		return
	}

	defer func(sockets *server.Sockets) {

		err := sockets.Close()

		if err != nil {

			logger.Error(fmt.Sprintf("Error occurred while closing sockets %v\n", err))

		}
	}(&sockets)

	go sockets.Receive(receiverChannel)

	go sockets.Send(senderChannel)

	processData()
}

func processData() {

	defer func() {

		if err := recover(); err != nil {

			processData()
		}
	}()

	for {

		message := <-receiverChannel

		contexts, err := utils.Decode(message)

		if err != nil {

			logger.Fatal(fmt.Sprintf("Some error occurred while decoding %v\n", err))

			return
		}

		for _, context := range contexts {

			logger.Info(fmt.Sprintf("Context: %s", context))

			if context[RequestType] == Discover {

				logger.Info(fmt.Sprintf("Request Type of the context is discovery"))

				go plugin.Discovery(context, senderChannel)

			} else if context[RequestType] == Collector {

				logger.Info(fmt.Sprintf("Request Type of the context is collect"))

				go plugin.Collect(context, senderChannel)

			}

		}

	}

}
