package com.slimming.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;
import org.orman.mapper.annotation.Column;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.PrimaryKey;
import org.orman.sql.C;

@Entity
public class Weight extends Model<Weight> {

	@Column(name="id", type="integer")
	@PrimaryKey(autoIncrement=true)
	public Integer id;

	@Column(name="date", type="date")
	public Date date;
	
	@Column(name="value", type="decimal(5,2)")
	public Double value;
	
	@Column(name="comments", type="varchar(100)")
	public String comments;
	
	private transient List<String> errors = new ArrayList<String>();
	
	public Weight() {
		
	}
	
	public Weight(Integer id, Date date, Double value, String comments) {
		this.id = id;
		this.date = date;
		this.value = value;
		this.comments = comments;
	}
	
	public boolean isNewRecord() {
		return this.id == null;
	}
	
	public boolean isValid() {
		this.getErrors().clear();
		if (date == null) {
			getErrors().add("Data não é válida");
		} else if (isNewRecord() && Weight.fetchSingle(ModelQuery.select().from(Weight.class).where(C.eq("date", this.date)).getQuery(), Weight.class) != null) {
			getErrors().add("Data já foi informada");
		}
		if (value == null || Double.isNaN(value) || value <= 0) {
			getErrors().add("Peso não é válido");
		}
		return getErrors().isEmpty();
	}
	
	public List<String> getErrors() {
		return this.errors;
	}
	
	public void insertOrUpdate() {
		if (this.isNewRecord()) {
			this.insert();
		} else {
			this.update();
		}
	}

	public static List<Weight> all() {
		return Weight.fetchQuery(ModelQuery.select().from(Weight.class).orderBy("Weight.date").getQuery(), Weight.class);
	}
	
	public static Weight find(int id) {
		return Weight.fetchSingle(ModelQuery.select().from(Weight.class).where(C.eq("id", id)).getQuery(), Weight.class);
	}
}