package ch.chassaing.hack.vm;

import ch.chassaing.hack.vm.command.Command;
import ch.chassaing.hack.vm.command.Function;

import java.util.List;

public class OptimizingByteCode
    implements ByteCode
{
    private final ByteCode delegate;

    public OptimizingByteCode(ByteCode delegate) {this.delegate = delegate;}

    @Override
    public void startVmFile(String filename) {delegate.startVmFile(filename);}

    @Override
    public void add(Command command) {

        delegate.add(command);

        if (command instanceof Function function) {
            System.out.println(function + " called");
        }

    }

    @Override
    public List<Command> commands() {

        return delegate.commands();
    }
}
