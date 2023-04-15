package ch.chassaing.hack.vm;

import ch.chassaing.hack.vm.command.Command;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class PlainByteCode
implements ByteCode
{
    private String filename;
    private final List<Command> commands = new LinkedList<>();

    @Override
    public void startVmFile(String filename)
    {
        if (StringUtils.isBlank(filename)) {
            throw new IllegalArgumentException("filename must not be blank");
        }
        this.filename = filename;
    }

    @Override
    public void add(Command command)
    {
        Objects.requireNonNull(command);
        if (filename == null) {
            throw new IllegalStateException("filename must be set prior to adding commands");
        }
        commands.add(command);
    }

    @Override
    public List<Command> commands()
    {
        return Collections.unmodifiableList(commands);
    }
}
