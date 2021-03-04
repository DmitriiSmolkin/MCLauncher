package biz.minecraft.launcher.entity.client.minecraft.argument;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArgumentsFromStringDeserializer implements JsonDeserializer<Map<ArgumentType, List<Argument>>> {

    @Override
    public Map<ArgumentType, List<Argument>> deserialize(JsonElement jElement, Type typeOfT, JsonDeserializationContext context) {

        // Minecraft Arguments String example: "--username ${auth_player_name} --version ${version_name} ..."

        Map<ArgumentType, List<Argument>> arguments = new HashMap<>();
        List<Argument> argumentsList = new ArrayList<>();

        String minecraftArgumentsString = jElement.getAsString();
        String[] minecraftArguments = minecraftArgumentsString.split(" ");

        for (String minecraftArgument : minecraftArguments) {
            String[] values = {minecraftArgument};
            Argument argumentItem = new Argument(values);
            argumentsList.add(argumentItem);
        }

        arguments.put(ArgumentType.GAME, argumentsList);

        return arguments;
    }

}
