@org.hibernate.annotations.GenericGenerator(name = "fine-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "fine_code_seq"),
			@org.hibernate.annotations.Parameter(name = "initial_value", value = "200010")
				}) package vaulsys.entity;
