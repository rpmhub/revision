package dev.rpmhub.revision.mappers.github;

public class CommitData {

    private String sha;

    private Commit commit;

    private Author author;

    public String getSha() {
        return sha;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Commit getCommit() {
        return commit;
    }

    public void setCommit(Commit commit) {
        this.commit = commit;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

}
