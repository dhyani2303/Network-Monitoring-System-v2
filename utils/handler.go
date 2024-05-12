package utils

import (
	"PluginEngine/constants"
)

func ErrorHandler(context map[string]interface{}, errorCode string, err error) {

	var errorArray []map[string]interface{}

	error := make(map[string]interface{})

	error[constants.Error] = err

	error[constants.ErrorCode] = errorCode

	error[constants.ErrorMessage] = err.Error()

	errorArray = append(errorArray, error)

	context[constants.Error] = errorArray

	context[constants.Status] = constants.Fail

	return

}
