class Foto < ApplicationRecord
  #attr_accessible :armario_id, :nome, :image
  belongs_to :imageable, polymorphic: true, required: false
  mount_uploader :image, ImageUploader
  mount_base64_uploader :image, ImageUploader
end
