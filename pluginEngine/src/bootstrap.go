package main

import (
	. "PluginEngine/src/consts"
	"PluginEngine/src/plugin"
	"PluginEngine/src/server"
	"PluginEngine/src/utils"
	"fmt"
)

var logger = utils.NewLogger("main", "bootstrap")

func main() {

	logger.Info("New Request has come to plugin engine")

	senderChannel := make(chan string, 20)

	receiverChannel := make(chan string, 20)

	channel := make(chan map[string]interface{}, 1)

	defer func() {

		if err := recover(); err != nil {

			logger.Fatal(fmt.Sprintf("Some panic occurred %v\n", err))

		}

		close(receiverChannel)

		close(senderChannel)

		close(channel)

	}()

	go server.Receive(receiverChannel)

	go server.Send(senderChannel)

	for {
		select {

		case message := <-receiverChannel:

			contexts, err := utils.Decode(message)

			if err != nil {

				logger.Fatal(fmt.Sprintf("Some error occurred while decoding %v\n", err))

				return
			}

			size := len(contexts)

			for _, context := range contexts {

				logger.Info(fmt.Sprintf("Context: %s", context))

				if context[RequestType] == Discover {

					logger.Info(fmt.Sprintf("Request Type of the context is discovery"))

					go plugin.Discovery(context, channel)

				} else if context[RequestType] == Collector {

					logger.Info(fmt.Sprintf("Request Type of the context is collect"))

					go plugin.Collect(context, channel)

				}

			}

			for size > 0 {

				select {

				case result := <-channel:

					encodedResult, err := utils.Encode(result)

					if err != nil {

						logger.Fatal(fmt.Sprintf("Some error occurred while encoding %v\n", err))

						return
					}

					logger.Trace(fmt.Sprintf("Message has been sent over the channel %v\n", encodedResult))

					senderChannel <- encodedResult

					size--
				}
			}
		}

	}
}
