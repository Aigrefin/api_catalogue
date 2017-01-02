package application.infrastructure.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Table(
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"name", "version"})
)
@Entity
public class Api {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;

    private String version;

    private String specificationPath;

    private String specificationType;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = SpecificationFile.class)
    @JoinTable(name = "API_SPECIFICATIONFILE", joinColumns = {@JoinColumn(name = "API_ID")}, inverseJoinColumns = {@JoinColumn(name = "SPECIFICATIONFILE_ID")})
    private SpecificationFile specificationFile;

    @JsonCreator
    public Api() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSpecificationPath() {
        return specificationPath;
    }

    public void setSpecificationPath(String specificationPath) {
        this.specificationPath = specificationPath;
    }

    public String getSpecificationType() {
        return specificationType;
    }

    public void setSpecificationType(String specificationType) {
        this.specificationType = specificationType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public SpecificationFile getSpecificationFile() {
        return specificationFile;
    }

    public void setSpecificationFile(SpecificationFile specificationFile) {
        this.specificationFile = specificationFile;
    }
}
