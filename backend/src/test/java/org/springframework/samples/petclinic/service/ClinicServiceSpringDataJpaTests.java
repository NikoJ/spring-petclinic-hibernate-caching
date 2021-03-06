package org.springframework.samples.petclinic.service;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import com.github.database.rider.spring.api.DBRider;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.util.EntityUtils;
import org.springframework.samples.petclinic.util.PostgresqlDbBaseTest;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test of the Service and the Repository layer.
 * <p>
 * ClinicServiceSpringDataJpaTests subclasses benefit from the following services provided by the Spring TestContext Framework:
 * </p> <ul> <li><strong>Spring IoC container caching</strong> which spares us unnecessary set up time between test
 * execution.</li> <li><strong>Dependency Injection</strong> of test fixture instances, meaning that we don't need to perform
 * application context lookups. See the use of {@link Autowired @Autowired} on the <code>{@link
 * ClinicServiceSpringDataJpaTests#clinicService clinicService}</code> instance variable, which uses autowiring <em>by type</em>.
 * <li><strong>Transaction management</strong>, meaning each test method is executed in its own transaction, which is
 * automatically rolled back by default. Thus, even if tests insert or otherwise change database state, there is no need for a
 * teardown or cleanup script. <li> An {@link org.springframework.context.ApplicationContext ApplicationContext} is also inherited
 * and can be used for explicit bean lookup if necessary. </li> </ul>
 *
 * @author Ken Krebs
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Igor Dmitriev
 */

@DBRider
@Disabled
@ActiveProfiles("test")
@DataJpaTest(includeFilters = @ComponentScan.Filter(Service.class))
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ClinicServiceSpringDataJpaTests extends PostgresqlDbBaseTest {

  @SuppressWarnings("unused")
  public ConnectionHolder connectionHolder = () -> dataSource.getConnection();

  @Autowired
  protected ClinicService clinicService;

  @Test
  @DataSet(
      value = {"datasets/clinic-service.xml"},
      executeScriptsBefore = "datasets/cleanup.sql",
      strategy = SeedStrategy.INSERT
  )
  public void shouldFindOwnersByLastName() {
    Collection<Owner> owners = this.clinicService.findOwnerByLastName("Davis");
    assertThat(owners.size()).isEqualTo(2);

    owners = this.clinicService.findOwnerByLastName("Daviss");
    assertThat(owners.isEmpty()).isTrue();
  }

  @Test
  @DataSet(
      value = {"datasets/clinic-service.xml"},
      executeScriptsBefore = "datasets/cleanup.sql",
      strategy = SeedStrategy.INSERT
  )
  public void shouldFindSingleOwnerWithPet() {
    Owner owner = this.clinicService.findOwnerById(100);
    assertThat(owner.getLastName()).startsWith("Franklin");
    assertThat(owner.getPets().size()).isEqualTo(1);
    assertThat(owner.getPets().get(0).getType()).isNotNull();
    assertThat(owner.getPets().get(0).getType().getName()).isEqualTo("cat");
  }

  @Test
  @Transactional
  @DataSet(
      value = {"datasets/clinic-service.xml"},
      executeScriptsBefore = "datasets/cleanup.sql",
      strategy = SeedStrategy.INSERT
  )
  public void shouldInsertOwner() {
    Collection<Owner> owners = this.clinicService.findOwnerByLastName("Schultz");
    int found = owners.size();

    Owner owner = new Owner();
    owner.setFirstName("Sam");
    owner.setLastName("Schultz");
    owner.setAddress("4, Evans Street");
    owner.setCity("Wollongong");
    owner.setTelephone("4444444444");
    this.clinicService.saveOwner(owner);
    assertThat(owner.getId().longValue()).isNotEqualTo(0);

    owners = this.clinicService.findOwnerByLastName("Schultz");
    assertThat(owners.size()).isEqualTo(found + 1);
  }

  @Test
  @Transactional
  @DataSet(
      value = {"datasets/clinic-service.xml"},
      executeScriptsBefore = "datasets/cleanup.sql",
      strategy = SeedStrategy.INSERT
  )
  public void shouldUpdateOwner() {
    Owner owner = this.clinicService.findOwnerById(100);
    String oldLastName = owner.getLastName();
    String newLastName = oldLastName + "X";

    owner.setLastName(newLastName);
    this.clinicService.saveOwner(owner);

    // retrieving new name from database
    owner = this.clinicService.findOwnerById(100);
    assertThat(owner.getLastName()).isEqualTo(newLastName);
  }

  @Test
  @DataSet(
      value = {"datasets/clinic-service.xml"},
      executeScriptsBefore = "datasets/cleanup.sql",
      strategy = SeedStrategy.INSERT
  )
  public void shouldFindPetWithCorrectId() {
    Pet pet7 = this.clinicService.findPetById(106);
    assertThat(pet7.getName()).startsWith("Samantha");
    assertThat(pet7.getOwner().getFirstName()).isEqualTo("Jean");

  }

  @Test
  @DataSet(
      value = {"datasets/clinic-service.xml"},
      executeScriptsBefore = "datasets/cleanup.sql",
      strategy = SeedStrategy.INSERT
  )
  public void shouldFindAllPetTypes() {
    Collection<PetType> petTypes = this.clinicService.findPetTypes();

    PetType petType1 = EntityUtils.getById(petTypes, PetType.class, 1);
    assertThat(petType1.getName()).isEqualTo("cat");
    PetType petType4 = EntityUtils.getById(petTypes, PetType.class, 4);
    assertThat(petType4.getName()).isEqualTo("snake");
  }

  @Test
  @Transactional
  @Disabled // ignored just to show UI tests coverage impact
  public void shouldInsertPetIntoDatabaseAndGenerateId() {
    Owner owner6 = this.clinicService.findOwnerById(6);
    int found = owner6.getPets().size();

    Pet pet = new Pet();
    pet.setName("bowser");
    Collection<PetType> types = this.clinicService.findPetTypes();
    pet.setType(EntityUtils.getById(types, PetType.class, 2));
    pet.setBirthDate(LocalDate.now());
    owner6.addPet(pet);
    assertThat(owner6.getPets().size()).isEqualTo(found + 1);

    this.clinicService.savePet(pet);
    this.clinicService.saveOwner(owner6);

    owner6 = this.clinicService.findOwnerById(6);
    assertThat(owner6.getPets().size()).isEqualTo(found + 1);
    // checks that id has been generated
    assertThat(pet.getId()).isNotNull();
  }

  @Test
  @Transactional
  @Disabled // ignored just to show UI tests coverage impact
  public void shouldUpdatePetName() throws Exception {
    Pet pet7 = this.clinicService.findPetById(7);
    String oldName = pet7.getName();

    String newName = oldName + "X";
    pet7.setName(newName);
    this.clinicService.savePet(pet7);

    pet7 = this.clinicService.findPetById(7);
    assertThat(pet7.getName()).isEqualTo(newName);
  }

  @Test
  @DataSet(
      value = {"datasets/clinic-service.xml"},
      executeScriptsBefore = "datasets/cleanup.sql",
      strategy = SeedStrategy.INSERT
  )
  public void shouldFindVets() {
    Collection<Vet> vets = this.clinicService.findVets();

    Vet vet = EntityUtils.getById(vets, Vet.class, 102);
    assertThat(vet.getLastName()).isEqualTo("Douglas");
    assertThat(vet.getNrOfSpecialties()).isEqualTo(2);
    assertThat(vet.getSpecialties().get(0).getName()).isEqualTo("dentistry");
    assertThat(vet.getSpecialties().get(1).getName()).isEqualTo("surgery");
  }

  @Test
  @Transactional
  @Disabled // ignored just to show UI tests coverage impact
  public void shouldAddNewVisitForPet() {
    Pet pet7 = this.clinicService.findPetById(7);
    int found = pet7.getVisits().size();
    Visit visit = new Visit();
    pet7.addVisit(visit);
    visit.setDescription("test");
    this.clinicService.saveVisit(visit);
    this.clinicService.savePet(pet7);

    pet7 = this.clinicService.findPetById(7);
    assertThat(pet7.getVisits().size()).isEqualTo(found + 1);
    assertThat(visit.getId()).isNotNull();
  }

  @Test
  @DataSet(
      value = {"datasets/clinic-service.xml"},
      executeScriptsBefore = "datasets/cleanup.sql",
      strategy = SeedStrategy.INSERT
  )
  public void shouldFindVisitsByPetId() {
    Collection<Visit> visits = this.clinicService.findVisitsByPetId(106);
    assertThat(visits.size()).isEqualTo(2);
    Visit[] visitArr = visits.toArray(new Visit[visits.size()]);
    assertThat(visitArr[0].getPet()).isNotNull();
    assertThat(visitArr[0].getDate()).isNotNull();
    assertThat(visitArr[0].getPet().getId()).isEqualTo(106);
  }

}
