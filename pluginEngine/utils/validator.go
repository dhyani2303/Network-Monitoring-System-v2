package utils

import (
	"PluginEngine/constants"
	"PluginEngine/logger"
	"fmt"
	"reflect"
	"strconv"
	"time"
)

var log = logger.NewLogger("utils", "validator")

func SetPort(context map[string]interface{}) int {

	defer func() {

		if err := recover(); err != nil {

			log.Fatal(fmt.Sprintf("Some panic occurred %v\n", err))

		}

	}()
	if context[constants.PORT] == nil {

		context[constants.PORT] = constants.DEFAULT_PORT
	} else {

		portType := reflect.TypeOf(context[constants.PORT])

		if portType == reflect.TypeOf("") {

			port, _ := strconv.Atoi(context[constants.PORT].(string))

			return port
		}
	}
	return int(context[constants.PORT].(float64))
}

func ToString(data any) string {

	defer func() {

		if err := recover(); err != nil {

			log.Fatal(fmt.Sprintf("Some panic occurred %v\n", err))

		}

	}()

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

	defer func() {

		if err := recover(); err != nil {

			log.Fatal(fmt.Sprintf("Some panic occurred %v\n", err))

		}

	}()

	if context[constants.TIMEOUT] == nil {

		context[constants.TIMEOUT] = constants.DEFAULT

	} else {
		if timeOut, ok := (context[constants.TIMEOUT]).(float64); ok {

			context[constants.TIMEOUT] = time.Duration(timeOut * float64(time.Second))
		}
	}

	return context[constants.TIMEOUT].(time.Duration)
}
