package net.sknv.engine.graph;

import org.joml.Vector4f;

import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

public class Material implements Serializable {

	private static final Vector4f DEFAULT_COLOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
	private Vector4f ambientColor, diffuseColor, specularColor;
	private transient Optional<Texture> texture;
	private float reflectance;
	private Optional<WebColor> webColor;

	public Material() {
		this(DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR, Optional.empty(), 0);
	}

	public Material(WebColor color) {
		this(new Vector4f(color.getVector3f(), 1f), 0f);
		this.webColor = Optional.of(color);
	}

	public Material(WebColor color, float alpha, float reflectance) {
		this(new Vector4f(color.getVector3f(), alpha), reflectance);
		this.webColor = Optional.of(color);
	}

	public Material(Vector4f color, float reflectance) {
		this(color, color, color, Optional.empty(), reflectance);
	}

	public Material(Texture texture) {
		this(DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR, Optional.of(texture), 0);
	}

	public Material(Texture texture, float reflectance) {
		this(DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR, Optional.of(texture), reflectance);
	}

	public Material(Vector4f ambientColor, Vector4f diffuseColor, Vector4f specularColor, Optional<Texture> texture, float reflectance) {
		this.ambientColor = ambientColor;
		this.diffuseColor = diffuseColor;
		this.specularColor = specularColor;
		this.texture = texture;
		this.reflectance = reflectance;
		this.webColor = Optional.empty();
	}

	public Vector4f getAmbientColor() {
		return ambientColor;
	}

	public void setAmbientColor(Vector4f ambientColor) {
		this.ambientColor = ambientColor;
	}

	public Vector4f getDiffuseColor() {
		return diffuseColor;
	}

	public void setDiffuseColor(Vector4f diffuseColor) {
		this.diffuseColor = diffuseColor;
	}

	public Vector4f getSpecularColor() {
		return specularColor;
	}

	public void setSpecularColor(Vector4f specularColor) {
		this.specularColor = specularColor;
	}

	public Optional<Texture> getTexture() {
		return texture;
	}

	public void setTexture(Texture texture) {
		this.texture = Optional.of(texture);
	}

	public boolean isTextured() {
		return texture.isPresent();
	}

	public float getReflectance() {
		return reflectance;
	}

	public void setReflectance(float reflectance) {
		this.reflectance = reflectance;
	}

	@Override
	public String toString() {
		return "Material{" + "color=" + (webColor.isPresent() ? "Unknown" : webColor.toString()) + '}';
	}

	private void readObject(java.io.ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
		inputStream.defaultReadObject();
		try {
			String fileName = (String) inputStream.readObject();
			setTexture(new Texture(fileName));
		} catch (Exception e) {
		}
	}

	private void writeObject(java.io.ObjectOutputStream outputStream) throws IOException {
		outputStream.defaultWriteObject();
		texture.ifPresent(texture -> {
			try {
				outputStream.writeObject(texture.getFileName());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}
