/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010  Matteo Miraz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package testful.random;

import java.io.File;
import java.util.concurrent.Future;
import java.util.logging.Level;

import testful.coverage.CoverageInformation;
import testful.coverage.TrackerDatum;
import testful.model.Operation;
import testful.model.ReferenceFactory;
import testful.model.Test;
import testful.model.TestCluster;
import testful.model.TestSplitter;
import testful.model.TestSplitter.Listener;
import testful.runner.ClassFinder;
import testful.utils.ElementManager;
import testful.utils.SimpleEntry;

public class RandomTestSplit extends RandomTest {

	private final TestSplitter simplifier;

	public RandomTestSplit(boolean enableCache, File logDirectory, ClassFinder finder, TestCluster cluster, ReferenceFactory refFactory, long seed, TrackerDatum ... data) {
		super(enableCache, logDirectory, finder, cluster, refFactory, seed, data);

		simplifier = new TestSplitter(true, cluster, refFactory);
	}

	@Override
	public void work(long duration) {
		start = System.currentTimeMillis();
		stop = start + duration;

		simplifier.register(new Listener() {

			@Override
			public void notify(TestCluster cluster, ReferenceFactory refFactory, Operation[] ops) {
				try {
					Future<ElementManager<String, CoverageInformation>> fut = runner.execute(finder, true, new Test(cluster, refFactory, ops), data);
					tests.put(new SimpleEntry<Operation[], Future<ElementManager<String, CoverageInformation>>>(ops, fut));
				} catch(InterruptedException e) {
					logger.log(Level.WARNING, "Interrupted: " + e.getMessage(), e);
				}
			}
		});

		while(System.currentTimeMillis() < stop) {
			Operation op = Operation.randomlyGenerate(cluster, refFactory, random);
			simplifier.analyze(op);
		}

		simplifier.flush();
	}
}
