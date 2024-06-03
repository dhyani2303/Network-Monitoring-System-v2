package server

import (
	"PluginEngine/src/utils"
	"fmt"
	"github.com/pebbe/zmq4"
)

var logger = utils.NewLogger("servers", "receiver")

func Receive(channel chan string) {

	socket, err := zmq4.NewSocket(zmq4.PULL)

	if err != nil {

		logger.Fatal(fmt.Sprintf("error occurred while creating socket %s", err))

		return
	}

	defer func(socket *zmq4.Socket) {

		if socket != nil {

			err = socket.Close()
		}

		return

	}(socket)

	if err := socket.Connect("tcp://localhost:5587"); err != nil {

		logger.Fatal(fmt.Sprintf("error occurred while connecting to socket %s", err))

		return
	}

	defer func() {

		if err := recover(); err != nil {

			logger.Fatal(fmt.Sprintf("Some panic occurred %v\n", err))

		}

		close(channel)

		return
	}()

	for {
		message, err := socket.Recv(0)

		logger.Trace(fmt.Sprintf("new message has been received: %s", message))

		if err != nil {

			logger.Fatal(fmt.Sprintf("error occurred while receiving message from socket %s", err))

			return
		}

		channel <- message

		logger.Trace(fmt.Sprintf("Message has been sent over channel. Message : %v", message))

	}

	return
}

func Send(channel chan string) {

	socket, err := zmq4.NewSocket(zmq4.PUSH)

	defer func(socket *zmq4.Socket) {

		if socket != nil {

			err = socket.Close()
		}

		return

	}(socket)

	defer func() {

		if err := recover(); err != nil {

			logger.Fatal(fmt.Sprintf("Some panic occurred %v\n", err))
		}

		close(channel)

		return
	}()

	if err != nil {

		logger.Fatal(fmt.Sprintf("error occurred while creating socket %s", err))

		return
	}

	if err := socket.Connect("tcp://localhost:5588"); err != nil {

		logger.Fatal(fmt.Sprintf("error occurred while connecting to socket %s", err))

		return
	}

	for {
		message := <-channel

		if _, err := socket.Send(message, 0); err != nil {

			logger.Fatal(fmt.Sprintf("Error occurred while sending message to socket %s", err))

		}

		logger.Trace(fmt.Sprintf("Message has been sent over channel. Message : %v", message))
	}

	return

}
