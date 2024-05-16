package utils

import (
	"PluginEngine/constants"
	"reflect"
	"strconv"
	"time"
)

func ValidatePort(context map[string]interface{}) int {

	if context[constants.Port] == nil {

		context[constants.Port] = constants.DefaultPort
	} else {

		portType := reflect.TypeOf(context[constants.Port])

		if portType == reflect.TypeOf("") {

			port, _ := strconv.Atoi(context[constants.Port].(string))

			return port
		}
	}
	return int(context[constants.Port].(float64))
}

func ToString(data any) string {

	typeOfData := reflect.TypeOf(data)

	if typeOfData == reflect.TypeOf("") {

		return data.(string)

	} else if typeOfData == reflect.TypeOf(0) {

		return strconv.Itoa(data.(int))

	} else if typeOfData == reflect.TypeOf(float64(0)) {

		return strconv.FormatFloat(data.(float64), 'f', -1, 64)

	} else if typeOfData == reflect.TypeOf(float32(0)) {

		return strconv.FormatFloat(data.(float64), 'f', -1, 32)
	}

	return ""

}

func ValidateTimeOut(context map[string]interface{}) time.Duration {

	if context[constants.TimeOut] == nil {

		context[constants.TimeOut] = constants.DefaultTimeOut

	} else {
		if timeOut, ok := (context[constants.TimeOut]).(float64); ok {

			context[constants.TimeOut] = time.Duration(timeOut * float64(time.Second))
		}
	}

	return context[constants.TimeOut].(time.Duration)
}
