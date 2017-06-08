class CreateJoinTablePersonCommunity < ActiveRecord::Migration[5.1]
  def change
    create_join_table :people, :communities do |t|
      # t.index [:person_id, :community_id]
      # t.index [:community_id, :person_id]
    end
  end
end
