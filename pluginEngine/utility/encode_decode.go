package utility

import (
	"encoding/base64"
	"encoding/json"
	"log"
)

func Encode(context map[string]interface{}) string {

	stringToEncode, err := json.Marshal(context)

	if err != nil {

		log.Fatal(err)

		return ""

	}
	encoded := base64.StdEncoding.EncodeToString(stringToEncode)

	return encoded

}

func Decode(stringToDecode string) map[string]interface{} {

	decoded, err := base64.StdEncoding.DecodeString(stringToDecode)

	ctxMap := make(map[string]interface{})

	if err != nil {

		log.Fatal(err)

		return nil

	} else {

		err = json.Unmarshal(decoded, &ctxMap)

		if err != nil {

			log.Fatal(err)

			return nil
		}

	}

	return ctxMap

}
