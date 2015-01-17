/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import se.kth.karamel.backend.running.model.tasks.MakeSoloRbTask;

/**
 *
 * @author kamal
 */
public class MakeSoloRbSerializer implements JsonSerializer<MakeSoloRbTask> {

  @Override
  public JsonElement serialize(MakeSoloRbTask task, Type type, JsonSerializationContext context) {
    final JsonObject jsonObj = new JsonObject();
    jsonObj.add("status", context.serialize(task.getStatus()));
    jsonObj.add("name", context.serialize(task.getName()));
    return jsonObj;
  }

}