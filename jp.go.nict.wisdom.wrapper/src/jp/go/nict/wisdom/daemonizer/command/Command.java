package jp.go.nict.wisdom.daemonizer.command;

import java.io.IOException;

public interface Command<I, O> {

	public void put(I input) throws IOException;
	public O getNextResult() throws InterruptedException, IOException;
	public void start() throws IOException;
	public void close() throws IOException;
	public void clear();
}
