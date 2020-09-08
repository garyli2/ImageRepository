package dev.garyli.imagerepository.models;

public class DeleteRequest {
	private String[] imagesToDelete;

	public String[] getImagesToDelete() {
		return imagesToDelete;
	}

	public void setImagesToDelete(String[] imagesToDelete) {
		this.imagesToDelete = imagesToDelete;
	}
}
