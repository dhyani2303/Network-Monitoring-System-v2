package client

import (
	"PluginEngine/utility"
	"PluginEngine/utility/constants"
	"context"
	"fmt"
	"github.com/masterzen/winrm"
	"time"
)

var client *winrm.Client

func CreateConnection(data map[string]interface{}) (err error) {

	var endpointConfig *winrm.Endpoint

	utility.ValidatePort(data)

	endpointConfig = winrm.NewEndpoint(data[constants.IP].(string), int((data[constants.Port]).(float64)), false, true, nil, nil, nil, time.Second*30)

	client, err = winrm.NewClient(endpointConfig, data[constants.Username].(string), data[constants.Password].(string))

	fmt.Println(err)
	return err
}

func ExecuteCommand(command string) (string, error, int, string) {

	output, errorOutput, exitCode, err := client.RunPSWithContext(context.Background(), command)

	return output, err, exitCode, errorOutput

}
