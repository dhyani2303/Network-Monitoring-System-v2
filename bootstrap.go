package main

import (
	"PluginEngine/devices"
	"PluginEngine/utility"
	"PluginEngine/utility/constants"
	"fmt"
	"log"
	"os"
)

func main() {

	file, err := os.OpenFile("plugin_engine.log", os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0644)

	if err != nil {

		log.Println(err)

		return
	}
	defer file.Close()

	log.SetOutput(file)

	contextData := utility.Decode(os.Args[1])

	if contextData != nil {

		switch contextData[constants.DeviceType] {

		case constants.Windows:

			devices.Windows(contextData)

		}
	} else {

		log.Println("Some problem occurred while decoding the context")

		return
	}

	fmt.Println(utility.Encode(contextData))

}
