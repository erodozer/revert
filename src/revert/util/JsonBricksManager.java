package revert.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.kgp.core.AssetsManager;
import com.kgp.util.Vector2;

/**
 * Parses a CSV style Tiled Map XML File into a collision mask for levels
 * 
 * @author Nicholas Hydock
 */
public class JsonBricksManager extends BrickManager {

	private static String TMX_DIR = "Levels/";

	private int stepX = 32;
	private int stepY = 32;

	public static JsonBricksManager load(String file, Gson gson)
	{
		InputStream input = AssetsManager.getResource(TMX_DIR + file + ".json");
		Reader data = new InputStreamReader(input);
		return gson.fromJson(data, JsonBricksManager.class);
	}

	@Override
	public void display(Graphics2D g) {
		// Ignore
		// TMX files are just being used for collision, do not draw with them
		
		// TODO DEBUG DRAWING, PLEASE COMMENT OUT OF PRODUCTION VERSION
		g.setColor(Color.white);
		for (int x = 0, col = 0; col < numCols; col++, x += stepX)
			for (int y = 0, row = 0; row < numRows; row++, y+= stepY)
				if (collisionMask[col][row])
					g.fillRect(x, y, stepX, stepY);
	}

	@Override
	public int getBrickWidth() {
		return stepX;
	}

	@Override
	public int getBrickHeight() {
		return stepY;
	}

	@Override
	protected void update() {
		// TODO Auto-generated method stub
	}

	public static class JsonBricksDeserializer implements JsonDeserializer<JsonBricksManager> {
		public JsonBricksManager deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonBricksManager t = new JsonBricksManager();
			JsonObject data = json.getAsJsonObject();
			JsonArray layers = data.get("layers").getAsJsonArray();
			
			//first layer must be the collision layer
			JsonObject collisionLayer = layers.get(0).getAsJsonObject();
			
			//deserialize data as a collision mask
			int[] layerData = context.deserialize(collisionLayer.get("data"), int[].class);
			int width = collisionLayer.get("width").getAsInt();
			int height = collisionLayer.get("height").getAsInt();
			t.collisionMask = new boolean[width][height];
			t.numCols = width;
			t.numRows = height;
			for (int row = 0, i = 0; row < height; row++)
				for (int col = 0; col < width; col++, i++)
					t.collisionMask[col][row] = layerData[i] == 2;
			
			//second layer is an object of collision points
			JsonObject pointLayer = layers.get(1).getAsJsonObject();
			JsonArray points = pointLayer.get("objects").getAsJsonArray().get(0).getAsJsonObject().get("polyline").getAsJsonArray();
			
			//deserialize points into a list of spawn areas
			t.spawnPoints = new Vector2[points.size()];
			for (int i = 0; i < points.size(); i++)
			{
				Vector2 v = new Vector2();
				JsonObject point = points.get(i).getAsJsonObject();
				v.x = point.get("x").getAsFloat();
				v.y = point.get("y").getAsFloat();
				t.spawnPoints[i] = v;
			}
			
			//get world size
			t.stepX = data.get("tilewidth").getAsInt();
			t.stepY = data.get("tileheight").getAsInt();
			
			return t;
		}
	}
}