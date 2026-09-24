# 🧩 Contributing to Sootup

Thank you for your interest in contributing to **Sootup**!  
We welcome contributions of all kinds — from small bug fixes to major improvements and new features.

---

## 🚀 How to Contribute

### 1. Fork the Repository
Start by **forking** the Sootup repository to your own GitHub account.  
Click the **"Fork"** button at the top-right of the repository page.

### 2. Clone Your Fork
Clone your forked repository to your local machine:
```bash
git clone https://github.com/<your-username>/sootup.git
cd sootup
```

### 3. Create a New Branch
Create a branch for your feature or bug fix:
```bash
git checkout -b feature/your-feature-name
```
Use clear and descriptive branch names, such as:
- `feature/new-parser`
- `fix/null-pointer`
- `docs/update-readme`

### 4. Make Your Changes
Make your modifications and ensure that:
- All existing tests pass.
- New functionality is covered by tests.
- Code adheres to the **Sootup Coding Standards** (see below).

### 5. Commit Your Changes
Write **clear, concise, and descriptive** commit messages:
```bash
git commit -m "Fix: handle null pointer in CFG builder"
```

### 6. Push and Create a Pull Request
Push your branch to your fork:
```bash
git push origin feature/your-feature-name
```

Then, open a **Pull Request (PR)** from your branch to the **main Sootup repository’s `main` branch**.  
Include a short summary of what you changed, why, and any related issue references (e.g., *“Fixes #123”*).

---

## 🔍 Review Process

1. Your PR will be **reviewed by a Sootup maintainer**.
2. You may be asked to make adjustments before it can be merged.
3. Once approved, a maintainer will merge your changes into the main branch.

Please be patient and respectful during the review process — maintainers are volunteers too!

---

## 🧱 Coding Standards

To maintain code quality and consistency, please follow these standards:

### General
- Follow the **[Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)**.
- Write **clean, modular, and readable code**.
- Use **meaningful variable, method, and class names**.
- Avoid code duplication; prefer reusable methods or utilities.
- Add **Javadoc comments** for all public classes and methods.
- Keep methods **short and focused** — ideally under 30 lines.

### Testing
- Write **unit tests** for all new functionality.
- Use the existing test framework (e.g., JUnit 5).
- Ensure **100% test pass rate** before submitting your PR.
- Include **edge cases** and **negative tests** where applicable.

### Documentation
- Update documentation if your change affects the public API or usage.
- Add examples and explanations in `README.md` or `/docs` where helpful.
- Keep commit and PR descriptions concise but informative.

---

## 🧠 Tips for a Great Contribution

- Open an **issue** first if you plan a large or breaking change — to discuss design and implementation ideas.
- Keep PRs **small and focused** — they’re easier to review and merge.
- Stay consistent with the existing project structure and naming conventions.

---

## 🫶 Thank You

Your contributions help make **Sootup** better for everyone.  
We appreciate your time, effort, and collaboration!
