package utils

import (
	"PluginEngine/constants"
)

func ErrorHandler(errorCode string, errMessage string) map[string]interface{} {

	error := make(map[string]interface{})

	error[constants.ErrorCode] = errorCode

	error[constants.ErrorMessage] = errMessage

	return error

}
