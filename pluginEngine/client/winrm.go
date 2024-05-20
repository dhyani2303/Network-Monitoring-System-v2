package client

import (
	"PluginEngine/constants"
	"PluginEngine/logger"
	"PluginEngine/utils"
	"context"
	"fmt"
	"github.com/masterzen/winrm"
	"time"
)

type WinRmClient struct {
	ipAddress string

	password string

	username string

	port int

	timeout time.Duration

	logger logger.Logger
}

func (client *WinRmClient) SetContext(context map[string]interface{}) {

	defer func() {

		if err := recover(); err != nil {

			client.logger.Fatal(fmt.Sprintf("%s", err))
		}
	}()

	client.logger = logger.NewLogger("client", "WindowsClient")

	client.username = utils.ToString(context[constants.USERNAME])

	client.password = utils.ToString(context[constants.PASSWORD])

	client.ipAddress = utils.ToString(context[constants.IP_ADDRESS])

	client.port = utils.SetPort(context)

	client.timeout = utils.ValidateTimeOut(context)

}

func (client *WinRmClient) CreateConnection() (*winrm.Client, error) {

	defer func() {

		if err := recover(); err != nil {

			client.logger.Fatal(fmt.Sprintf("%s", err))
		}
	}()

	endpointConfig := winrm.NewEndpoint(client.ipAddress, client.port, false, true, nil, nil, nil, client.timeout)

	winRmClient, err := winrm.NewClient(endpointConfig, client.username, client.password)

	if err != nil {

		client.logger.Error(fmt.Sprintf("Error in creating winRmClient: %v\n", err))

		return nil, err
	}
	client.logger.Info(fmt.Sprintf("Successfully Created the client"))

	return winRmClient, err

}

func (client *WinRmClient) ExecuteCommand(winRmClient *winrm.Client, command string) (string, string, int, error) {

	defer func() {

		if err := recover(); err != nil {

			client.logger.Fatal(fmt.Sprintf("%s", err))
		}
	}()

	output, errorOutput, exitCode, err := winRmClient.RunPSWithContext(context.Background(), command)

	return output, errorOutput, exitCode, err

}
