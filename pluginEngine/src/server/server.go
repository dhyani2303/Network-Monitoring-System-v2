package server

import (
	"PluginEngine/src/utils"
	"fmt"
	"github.com/pebbe/zmq4"
)

var logger = utils.NewLogger("servers", "receiver")

type Sockets struct {
	sender, receiver *zmq4.Socket
}

func (sockets *Sockets) Init() (err error) {

	sockets.sender, err = zmq4.NewSocket(zmq4.PUSH)

	if err != nil {

		logger.Fatal(fmt.Sprintf("error occurred while creating  sender socket %s", err))

		return
	}
	if err = sockets.sender.Connect("tcp://localhost:5588"); err != nil {

		logger.Fatal(fmt.Sprintf("error occurred while connecting sender socket %s", err))

		return
	}

	sockets.receiver, err = zmq4.NewSocket(zmq4.PULL)

	if err != nil {

		logger.Fatal(fmt.Sprintf("error occurred while creating receiver socket %s", err))

		return
	}

	if err = sockets.receiver.Connect("tcp://localhost:5587"); err != nil {

		logger.Fatal(fmt.Sprintf("error occurred while connecting receiver socket %s", err))

		return
	}

	return

}

func (sockets *Sockets) Close() (err error) {

	if sockets.sender != nil {

		err = sockets.sender.Close()
	}
	if sockets.receiver != nil {

		err = sockets.receiver.Close()
	}

	return

}
func (sockets *Sockets) Receive(channel chan string) {

	defer func() {

		if err := recover(); err != nil {

			logger.Fatal(fmt.Sprintf("Some panic occurred %v\n", err))

		}

		sockets.Receive(channel)

	}()

	for {

		message, err := sockets.receiver.Recv(0)

		logger.Trace(fmt.Sprintf("new message has been received: %s", message))

		if err != nil {

			logger.Fatal(fmt.Sprintf("error occurred while receiving message from socket %s", err))

			return
		}

		channel <- message

	}

	return
}

func (sockets *Sockets) Send(channel chan string) {

	defer func() {

		if err := recover(); err != nil {

			logger.Fatal(fmt.Sprintf("Some panic occurred %v\n", err))
		}

		sockets.Send(channel)
	}()

	for {
		message := <-channel

		if _, err := sockets.sender.Send(message, 0); err != nil {

			logger.Fatal(fmt.Sprintf("Error occurred while sending message to socket %s", err))

		}

		logger.Trace(fmt.Sprintf("Message has been sent over channel. Message : %v", message))
	}

	return

}
