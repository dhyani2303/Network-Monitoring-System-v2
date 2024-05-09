package utility

import (
	"PluginEngine/utility/constants"
)

func ValidatePort(context map[string]interface{}) {

	if context[constants.Port] == nil {

		context[constants.Port] = constants.DefaultPort
	}
}
