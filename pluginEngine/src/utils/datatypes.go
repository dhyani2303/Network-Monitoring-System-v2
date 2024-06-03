package utils

import (
	"fmt"
	"reflect"
	"strconv"
)

var log = NewLogger("utils", "datatypes")

func ToString(data interface{}) string {

	defer func() {

		if err := recover(); err != nil {

			log.Fatal(fmt.Sprintf("Some panic occurred %v\n", err))

		}
		return

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

func ToInt(data interface{}) int {

	defer func() {

		if err := recover(); err != nil {

			log.Fatal(fmt.Sprintf("Some panic occurred %v\n", err))

		}
		return

	}()

	typeOfData := reflect.TypeOf(data)

	if typeOfData == reflect.TypeOf(float64(0)) {

		return int(data.(float64))

	} else if typeOfData == reflect.TypeOf("") {

		value, err := strconv.Atoi(data.(string))

		if err != nil {

			return 0
		}
		return value

	}

	return data.(int)
}
