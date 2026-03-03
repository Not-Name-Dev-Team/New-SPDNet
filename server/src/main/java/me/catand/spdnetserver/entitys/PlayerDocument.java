package me.catand.spdnetserver.entitys;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "player_documents")
public class PlayerDocument {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "player_id")
	private Long playerId;

	@Column(name = "document_type", nullable = false, length = 50)
	private String documentType;

	@Column(name = "page_name", nullable = false, length = 255)
	private String pageName;

	@Column(name = "state", nullable = false)
	private int state;
}
