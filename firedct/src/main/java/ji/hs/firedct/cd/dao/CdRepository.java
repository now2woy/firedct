package ji.hs.firedct.cd.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CdRepository extends JpaRepository<Cd, CdPrimaryKey> {
	public List<Cd> findByCls(String cls);
}
