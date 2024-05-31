package zmq

import (
	"PluginEngine/logger"
	"fmt"
	"github.com/pebbe/zmq4"
)

func Receive(channel chan string) {

	logger := logger.NewLogger("zmq", "receiver")

	pullSocket, err := zmq4.NewSocket(zmq4.PULL)

	defer func(responder *zmq4.Socket) {

		err := responder.Close()

		if err != nil {

			logger.Fatal(fmt.Sprintf("error occurred while closing pullSocket %s", err))

		}
	}(pullSocket)

	if err != nil {

		logger.Fatal(fmt.Sprintf("error occurred while creating pullSocket %s", err))

		return
	}

	err = pullSocket.Connect("tcp://localhost:5587")

	if err != nil {

		logger.Fatal(fmt.Sprintf("error occurred while connecting to pullSocket %s", err))

		return
	}

	defer func() {

		if err := recover(); err != nil {

			logger.Fatal(fmt.Sprintf("Some panic occurred %v\n", err))

		}

		close(channel)
	}()

	for {
		message, err := pullSocket.Recv(0)

		logger.Info(fmt.Sprintf("new message has been received: %s", message))

		if err != nil {

			logger.Fatal(fmt.Sprintf("error occurred while receiving message from pullSocket %s", err))

			return

		} else {
			channel <- message

			logger.Info(fmt.Sprintf("Message has been sent over channel. Message : %v", message))

		}
	}

}

func Send(channel chan string) {

	logger := logger.NewLogger("zmq", "sender")

	pushSocket, err := zmq4.NewSocket(zmq4.PUSH)

	defer func() {
		if err := recover(); err != nil {

			logger.Fatal(fmt.Sprintf("Some panic occurred %v\n", err))

		}

		close(channel)
	}()

	if err != nil {

		logger.Fatal(fmt.Sprintf("error occurred while creating pushSocket %s", err))

		return
	}
	err = pushSocket.Connect("tcp://localhost:5588")

	if err != nil {

		logger.Fatal(fmt.Sprintf("error occurred while connecting to pushSocket %s", err))

		return
	}

	for {
		message := <-channel
		_, err := pushSocket.Send(message, 0)

		if err != nil {

			logger.Fatal(fmt.Sprintf("Error occurred while sending message to pushSocket %s", err))

		}

		logger.Info(fmt.Sprintf("Message has been sent over channel. Message : %v", message))
	}

}
