package utils

import (
	"PluginEngine/src/consts"
	"encoding/base64"
	"encoding/json"
	"fmt"
)

var logger = NewLogger("utils", "util")

func ErrorHandler(errorCode string, errMessage string) (err map[string]interface{}) {

	defer func() {

		if recovery := recover(); recovery != nil {

			logger.Fatal(fmt.Sprintf("Some panic occurred. Panic: %s", recovery))
		}

	}()

	err[consts.ErrorCode] = errorCode

	err[consts.ErrorMessage] = errMessage

	return

}

func Encode(resultMap map[string]interface{}) (result string, err error) {

	defer func() {

		if recovery := recover(); recovery != nil {

			logger.Fatal(fmt.Sprintf("Some panic occurred. Panic: %s", recovery))
		}

	}()

	jsonBytes, err := json.Marshal(resultMap)

	if err != nil {

		return "", err

	}

	result = base64.StdEncoding.EncodeToString(jsonBytes)

	return result, nil

}

func Decode(stringToDecode string) (result []map[string]interface{}, err error) {

	defer func() {

		if recovery := recover(); recovery != nil {

			logger.Fatal(fmt.Sprintf("Some panic occurred. Panic: %s", recovery))
		}

	}()

	decoded, err := base64.StdEncoding.DecodeString(stringToDecode)

	if err != nil {

		return nil, err

	}

	if err = json.Unmarshal(decoded, &result); err != nil {

		return nil, err
	}

	return result, nil

}
