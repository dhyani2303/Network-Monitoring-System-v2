package client

import (
	. "PluginEngine/src/consts"
	"PluginEngine/src/utils"
	"context"
	"fmt"
	"github.com/masterzen/winrm"
	"time"
)

var logger = utils.NewLogger("clients", "client")

type WinRmClient struct {
	error []error

	ipAddress, password, username string

	timeout float64

	port int
}

func (client *WinRmClient) SetContext(context map[string]interface{}) {

	defer func() {

		if err := recover(); err != nil {

			logger.Fatal(fmt.Sprintf("%s", err))
		}
	}()

	client.username = utils.ToString(context[Username])

	client.password = utils.ToString(context[Password])

	client.ipAddress = utils.ToString(context[IpAddress])

	client.port = DefaultPort

	client.timeout = DefaultTimeOut

	if _, ok := context[Port]; ok {

		client.port = utils.ToFloat(context[Port])

	}

	if _, ok := context[Timeout]; ok {

		if timeOut, ok := (context[Timeout]).(float64); ok {

			client.timeout = timeOut
		}
	}
}

func (client *WinRmClient) CreateConnection() (*winrm.Client, error) {

	defer func() {

		if err := recover(); err != nil {

			logger.Fatal(fmt.Sprintf("%s", err))

			client.error = append(client.error, fmt.Errorf("%v", err))
		}
	}()

	config := winrm.NewEndpoint(client.ipAddress, client.port, false, true, nil, nil, nil, time.Duration(client.timeout*float64(time.Second)))

	winRmClient, err := winrm.NewClient(config, client.username, client.password)

	if err != nil {

		logger.Error(fmt.Sprintf("Error in creating winRmClient: %v\n", err))

		return nil, err
	}
	logger.Info(fmt.Sprintf("Successfully Created the clients"))

	return winRmClient, err

}

func (client *WinRmClient) ExecuteCommand(winRmClient *winrm.Client, command string) (string, string, int, error) {

	defer func() {

		if err := recover(); err != nil {

			logger.Fatal(fmt.Sprintf("%s", err))
		}
	}()

	//The logic to remove these 4 return values is yet to be done
	output, errorOutput, exitCode, err := winRmClient.RunPSWithContext(context.Background(), command)

	return output, errorOutput, exitCode, err

}

func (client *WinRmClient) getErrors() []error {

	return client.error

}
