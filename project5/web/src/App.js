import "./App.css"
import { MantineProvider } from "@mantine/core"
import { Button } from "@mantine/core"
import { AppShell, Navbar, Header } from "@mantine/core"
import { IconBoxSeam, IconUser, IconCheckupList } from "@tabler/icons"
import { ThemeIcon, UnstyledButton, Group, Text } from "@mantine/core"
import React, { useState } from "react"
import CustomerView from "./CustomerView"
import AdminView from "./AdminView"
import DeliveryView from "./DeliveryView"

function MainLink({ icon, color, label, selected, onClick }) {
    return (
        <UnstyledButton
            sx={(theme) => ({
                display: "block",
                width: "100%",
                padding: theme.spacing.xs,
                borderRadius: theme.radius.sm,
                color: theme.black,
                backgroundColor: selected ? theme.colors[color][2] : "",
                "&:hover": {
                    backgroundColor: selected
                        ? theme.colors[color][2]
                        : theme.colors.gray[2],
                },
            })}
            onClick={onClick}
        >
            <Group>
                <ThemeIcon color={color} variant="light">
                    {icon}
                </ThemeIcon>

                <Text size="sm">{label}</Text>
            </Group>
        </UnstyledButton>
    )
}
function App() {
    const [view, setView] = useState("customer")

    let mainBody
    switch (view) {
        case "customer":
            mainBody = <CustomerView />
            break
        case "admin":
            mainBody = <AdminView />
            break
        case "delivery":
            mainBody = <DeliveryView />
            break
    }

    return (
        <MantineProvider withGlobalStyles withNormalizeCSS>
            <AppShell
                padding="md"
                navbar={
                    <Navbar width={{ base: 300 }} p="xs">
                        <MainLink
                            icon={<IconUser size={16} />}
                            color="green"
                            label="Customer"
                            key="customer"
                            selected={view === "customer"}
                            onClick={() => setView("customer")}
                        />
                        <MainLink
                            icon={<IconCheckupList size={16} />}
                            color="blue"
                            label="Admin"
                            key="admin"
                            selected={view === "admin"}
                            onClick={() => setView("admin")}
                        />
                        <MainLink
                            icon={<IconBoxSeam size={16} />}
                            color="orange"
                            label="Delivery"
                            key="delivery"
                            selected={view === "delivery"}
                            onClick={() => setView("delivery")}
                        />
                    </Navbar>
                }
                header={
                    <Header height={60} p="xs">
                        Poli Eat
                    </Header>
                }
                styles={(theme) => ({
                    main: {
                        backgroundColor:
                            theme.colorScheme === "dark"
                                ? theme.colors.dark[8]
                                : theme.colors.gray[0],
                    },
                })}
            >
                {mainBody}
            </AppShell>
        </MantineProvider>
    )
}

export default App
