package com.hiretrack.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "pipeline_stages")
@Getter
@Setter
@NoArgsConstructor
public class PipelineStage {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "is_terminal", nullable = false)
    private boolean terminal = false;

    public PipelineStage(Job job, String name, int position, boolean terminal) {
        this.job = job;
        this.name = name;
        this.position = position;
        this.terminal = terminal;
    }
}
