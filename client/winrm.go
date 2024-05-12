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
	ip string

	password string

	username string

	port int

	timeout time.Duration

	logger logger.Logger
}

func (client *WinRmClient) SetContext(context map[string]interface{}) {

	client.logger = logger.NewLogger("client", "WindowsClient")

	client.username = utils.ToString(context[constants.Username])

	client.password = utils.ToString(context[constants.Password].(string))

	client.ip = utils.ToString(context[constants.IP].(string))

	client.port = utils.ValidatePort(context)

	client.timeout = utils.ValidateTimeOut(context)

}

func (client *WinRmClient) CreateConnection() (*winrm.Client, error) {

	endpointConfig := winrm.NewEndpoint(client.ip, client.port, false, true, nil, nil, nil, client.timeout)

	winRmClient, err := winrm.NewClient(endpointConfig, client.username, client.password)

	if err != nil {

		client.logger.Error(fmt.Sprintf("Error creating winRmClient: %v\n", err))

		return nil, err
	}
	client.logger.Info(fmt.Sprintf("Successfully Created the client"))

	return winRmClient, err

}

func (client *WinRmClient) ExecuteCommand(winRmClient *winrm.Client, command string) (string, string, int, error) {

	output, errorOutput, exitCode, err := winRmClient.RunPSWithContext(context.Background(), command)

	return output, errorOutput, exitCode, err

}
