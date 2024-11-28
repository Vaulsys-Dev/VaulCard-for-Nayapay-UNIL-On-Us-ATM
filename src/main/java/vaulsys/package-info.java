@org.hibernate.annotations.GenericGenerator(name = "switch-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "switch_seq")
				}) 
package vaulsys;
