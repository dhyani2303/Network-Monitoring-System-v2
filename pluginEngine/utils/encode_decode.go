package utils

import (
	"PluginEngine/logger"
	"encoding/base64"
	"encoding/json"
	"fmt"
)

func Encode(resultMap map[string]interface{}) (string, error) {

	logger := logger.NewLogger("utils", "encode")

	jsonBytes, err := json.Marshal(resultMap)

	if err != nil {

		logger.Error(fmt.Sprintf("Error in Marshalling Map to Json:%v\n", err))

		return "", err
	}

	encodedResult := base64.StdEncoding.EncodeToString(jsonBytes)

	logger.Info(fmt.Sprintf("Encoding successful\n"))

	return encodedResult, nil

}

func Decode(stringToDecode string) ([]map[string]interface{}, error) {

	logger := logger.NewLogger("utils", "decode")

	decoded, err := base64.StdEncoding.DecodeString(stringToDecode)

	var ctxMap []map[string]interface{}

	if err != nil {

		logger.Error(fmt.Sprintf("Error in decoding base64 string : %v", err))

		return nil, err

	}

	err = json.Unmarshal(decoded, &ctxMap)

	if err != nil {

		logger.Error(fmt.Sprintf("Error occurred while unmarshalling context: %v", err))

		return nil, err
	}

	logger.Info("Decoded & Marshalled  context successfully")

	return ctxMap, nil

}
