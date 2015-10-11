import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Vlasov Alexander
 * Date: 09.07.2014
 * Time: 19:01
 * To change this template use File | Settings | File Templates.
 *
 * @author Alexander Vlasov
 */
public class Branch {
    private int id;
    private List<Branch> branches;
    private long branchesSize;
    private long leafsSize;
    private long size;
    private Path path;
    private Branch parent;

    public Branch(Path path) {
        this(path, 0, null);
    }

    public Branch(Path path, int id, Branch parent) {
        if (isFirstChild(path, parent)) {
            System.out.print("Обрабатываю " + path + " и его подкаталоги...   ");
        }
        this.path = path;
        branches = new ArrayList<>();
        leafsSize = 0;
        branchesSize = 0;
        addFiles();
        size = branchesSize + leafsSize;
        this.id = id;
        this.parent = parent;
        if (isFirstChild(path, parent)) {
            int kB = 1024;
            int mB = 1024 * 1024;
            int gB = mB * 1024;
            String size$ = String.valueOf(size > gB ? size / gB + " GB" : size > mB ? size / mB + " mB" : size > kB ? size / kB + " kB" : size + " bytes");

            System.out.println(size$);
        }

    }

    private boolean isFirstChild(Path path, Branch parent) {
        return parent != null && Files.isDirectory(path) && DiscDiagram.homeDirectory.equals(path.getParent());
    }

    protected void addFiles() {
        try {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
//                System.out.println(entries.getClass().getName());
                for (Path file : entries) {
                    add(file);
                }
            } catch (AccessDeniedException ex) {
                // Exception suppress!!!!
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void add(Path path) {
        if (Files.isDirectory(path)) {
            Branch branch = new Branch(path, branches.size() + 1, this);
            branches.add(branch);
            branchesSize += branch.size;

        } else if (path.toFile().isFile()) {
            leafsSize += path.toFile().length();
        }
    }

    public Branch getParent() {
        return parent;
    }

    public String getName() {
        int maxLength = 30;
        String name = path.getFileName().toString();
        if (name.length() > maxLength) name = name.substring(0, maxLength - 3).concat("...");
        return id + ". " + name;
    }

    public List<Branch> getBranches() {
        return branches;
    }

    public long getLeafsSize() {
        return leafsSize;
    }

    public long getSize() {
        return size;
    }

    public Path getPath() {
        return path;
    }

    public int getId() {
        return id;
    }


    @Override
    public String toString() {
        return path.toString();
    }

    public Branch getBranchByName(String name) {
        String id$ = name.split("\\.")[0];
        if (id$.matches("\\d*")) {
            int id = Integer.parseInt(id$);
            for (Branch branch : branches) {
                if (branch.getId() == id) return branch;
            }
        }
        return null;
    }
}
