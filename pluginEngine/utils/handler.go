package utils

import (
	"PluginEngine/constants"
	"PluginEngine/logger"
	"fmt"
)

func ErrorHandler(errorCode string, errMessage string) map[string]interface{} {

	logger := logger.NewLogger("utils", "handler")

	defer func() {

		recovery := recover()

		if recovery != nil {

			logger.Fatal(fmt.Sprintf("Some panic occurred. Panic: %s", recovery))
		}
	}()

	err := make(map[string]interface{})

	err[constants.ERROR_CODE] = errorCode

	err[constants.ERROR_MESSAGE] = errMessage

	return err

}
