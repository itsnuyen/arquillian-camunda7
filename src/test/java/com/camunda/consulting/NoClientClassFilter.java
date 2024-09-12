package com.camunda.consulting;

import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;

import java.util.ArrayList;
import java.util.List;

public class NoClientClassFilter implements Filter<ArchivePath> {
	
	public static final List<String> EXCLUSION_PATHS = new ArrayList<String>();
	
	static {
		EXCLUSION_PATHS.add("/com/ing/diba/bpe/ActivityInfoImpl");
	}

	private Filter<ArchivePath> chain = null;
	
	public NoClientClassFilter(Filter<ArchivePath> chain) {
		super();
		this.chain = chain;
	}

	public NoClientClassFilter() {
		super();
	}

	@Override
	public boolean include(ArchivePath arg0) {
		for (String exclusion : EXCLUSION_PATHS) {
			if (arg0.get().startsWith(exclusion)) {
				return false;
			}
		}
		if (chain!=null) {
			return chain.include(arg0);
		} 
		return true;
	}
}
