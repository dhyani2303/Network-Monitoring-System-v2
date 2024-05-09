package utility

import "PluginEngine/utility/constants"

func ErrorHandler(context map[string]interface{}, errorCode string, err error) {

	error := make(map[string]interface{})

	error[constants.Error] = err

	error[constants.ErrorCode] = constants.DISCOVERYERROR

	error[constants.ErrorMessage] = err.Error()

	context[constants.Error] = error

	context[constants.Status] = constants.Fail

	return

}
